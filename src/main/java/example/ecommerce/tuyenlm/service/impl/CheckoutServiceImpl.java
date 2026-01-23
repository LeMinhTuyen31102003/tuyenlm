package example.ecommerce.tuyenlm.service.impl;

import example.ecommerce.tuyenlm.dto.request.CheckoutRequest;
import example.ecommerce.tuyenlm.dto.request.LockInventoryRequest;
import example.ecommerce.tuyenlm.dto.request.UnlockInventoryRequest;
import example.ecommerce.tuyenlm.dto.response.CheckoutResponse;
import example.ecommerce.tuyenlm.dto.response.LockInventoryResponse;
import example.ecommerce.tuyenlm.entity.*;
import example.ecommerce.tuyenlm.enums.OrderStatus;
import example.ecommerce.tuyenlm.enums.ReservationStatus;
import example.ecommerce.tuyenlm.exception.EmptyCartException;
import example.ecommerce.tuyenlm.exception.InsufficientStockException;
import example.ecommerce.tuyenlm.exception.ResourceNotFoundException;
import example.ecommerce.tuyenlm.exception.BadRequestException;
import example.ecommerce.tuyenlm.repository.*;
import example.ecommerce.tuyenlm.service.inter.ICheckoutService;
import example.ecommerce.tuyenlm.service.inter.IEmailService;
import example.ecommerce.tuyenlm.service.inter.IInventoryReservationService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements ICheckoutService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository variantRepository;
    private final InventoryReservationRepository reservationRepository;
    private final IInventoryReservationService reservationService;
    private final IEmailService emailService;
    private final EntityManager entityManager;

    @Value("${app.frontend.url:http://localhost:8080}")
    private String frontendUrl;

    private static final int RESERVATION_EXPIRATION_MINUTES = 15;

    /**
     * Lock inventory - trừ stock ngay khi người dùng bấm "Thanh toán"
     * Tạo reservation và giữ hàng trong 15 phút
     * Dùng READ_COMMITTED + Pessimistic Lock để tăng concurrency
     * Retry mechanism để xử lý lock contention
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public LockInventoryResponse lockInventory(LockInventoryRequest request) {
        log.info("Locking inventory for sessionId: {}", request.getSessionId());

        // 1. Find cart
        Cart cart = cartRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "sessionId", request.getSessionId()));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new EmptyCartException("Cannot lock inventory with empty cart");
        }

        // 2. Validate stock and create reservations (TRỪ STOCK NGAY)
        List<InventoryReservation> reservations = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            // Dùng createReservationWithLock để FIX RACE CONDITION
            // Lock variant → validate → trừ stock → tạo reservation (tất cả trong 1
            // transaction với lock)
            InventoryReservation reservation = reservationService.createReservationWithLock(
                    cartItem.getVariant().getId(),
                    cartItem.getQuantity(),
                    RESERVATION_EXPIRATION_MINUTES);
            reservations.add(reservation);

            subtotal = subtotal.add(cartItem.getPriceSnapshot().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        // 3. Build response
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(RESERVATION_EXPIRATION_MINUTES);
        List<Long> reservationIds = reservations.stream()
                .map(InventoryReservation::getId)
                .collect(Collectors.toList());

        log.info("Locked inventory with {} reservations for sessionId: {}", reservationIds.size(),
                request.getSessionId());

        return LockInventoryResponse.builder()
                .reservationIds(reservationIds)
                .subtotal(subtotal)
                .shippingFee(BigDecimal.ZERO)
                .total(subtotal)
                .expiresAt(expiresAt)
                .message("Inventory locked successfully. Please complete checkout within 15 minutes.")
                .build();
    }

    /**
     * Unlock inventory - hoàn lại stock khi người dùng bấm Cancel
     */
    @Override
    @Transactional
    public void unlockInventory(UnlockInventoryRequest request) {
        log.info("Unlocking inventory for reservationIds: {}", request.getReservationIds());

        List<InventoryReservation> reservations = reservationRepository.findAllById(request.getReservationIds());

        if (reservations.isEmpty()) {
            throw new ResourceNotFoundException("No reservations found with provided IDs");
        }

        // Chỉ unlock những reservation đang ACTIVE
        List<InventoryReservation> activeReservations = reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.ACTIVE)
                .collect(Collectors.toList());

        if (activeReservations.isEmpty()) {
            log.warn("No active reservations to unlock");
            return;
        }

        // Release reservations (hoàn lại stock)
        reservationService.releaseReservations(activeReservations);

        log.info("Unlocked {} reservations successfully", activeReservations.size());
    }

    /**
     * Process checkout - tạo đơn hàng khi người dùng submit form
     * Sử dụng các reservation đã được tạo từ lockInventory
     * Dùng READ_COMMITTED để tăng concurrency
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CheckoutResponse processCheckout(CheckoutRequest request, List<Long> reservationIds) {
        log.info("Processing checkout for sessionId: {} with reservationIds: {}", request.getSessionId(),
                reservationIds);

        // 1. Find cart
        Cart cart = cartRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "sessionId", request.getSessionId()));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new EmptyCartException("Cannot checkout with empty cart");
        }

        // 2. Validate reservations
        if (reservationIds == null || reservationIds.isEmpty()) {
            throw new BadRequestException("Reservation IDs are required. Please lock inventory first.");
        }

        List<InventoryReservation> reservations = reservationRepository.findAllById(reservationIds);

        if (reservations.size() != reservationIds.size()) {
            throw new BadRequestException("Some reservations not found. Please lock inventory again.");
        }

        // Validate all reservations are ACTIVE and not expired
        LocalDateTime now = LocalDateTime.now();
        for (InventoryReservation reservation : reservations) {
            if (reservation.getStatus() != ReservationStatus.ACTIVE) {
                throw new BadRequestException("Reservation " + reservation.getId() + " is not active");
            }
            if (reservation.getExpiresAt().isBefore(now)) {
                throw new BadRequestException(
                        "Reservation " + reservation.getId() + " has expired. Please lock inventory again.");
            }
        }

        // 3. Calculate total
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            subtotal = subtotal.add(cartItem.getPriceSnapshot().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        // 4. Create order
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setTrackingToken(UUID.randomUUID().toString());
        order.setCustomerName(request.getCustomerName());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setSubtotal(subtotal);
        order.setShippingFee(BigDecimal.ZERO); // Can be calculated based on address
        order.setTotal(subtotal.add(order.getShippingFee()));
        order.setStatus(OrderStatus.PENDING);

        order = orderRepository.save(order);
        log.info("Created order: {}", order.getOrderNumber());

        // 5. Create order items
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setVariant(cartItem.getVariant());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceSnapshot(cartItem.getPriceSnapshot());
            orderItem.setSkuSnapshot(cartItem.getVariant().getSku());
            orderItem.setNameSnapshot(
                    cartItem.getVariant().getProduct().getName() + " - " +
                            cartItem.getVariant().getSize() + " - " +
                            cartItem.getVariant().getColor());
            orderItemRepository.save(orderItem);
        }

        // 6. Link reservations to order
        for (InventoryReservation reservation : reservations) {
            reservation.setOrder(order);
            reservationRepository.save(reservation);
        }

        // 7. Clear cart
        cartItemRepository.deleteByCartId(cart.getId());
        log.info("Cleared cart for sessionId: {}", request.getSessionId());

        // 8. Send order confirmation email
        try {
            // Flush to persist all changes to database before reloading
            entityManager.flush();
            // Clear persistence context to force fresh load from DB
            entityManager.clear();

            // Reload order with items (JOIN FETCH to avoid lazy loading)
            Order orderWithItems = orderRepository.findByIdWithItems(order.getId())
                    .orElse(order);
            log.info("Loaded order {} with {} items for email",
                    orderWithItems.getOrderNumber(),
                    orderWithItems.getItems() != null ? orderWithItems.getItems().size() : 0);
            emailService.sendOrderConfirmationEmail(orderWithItems);
            log.info("Order confirmation email sent for order: {}", order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email for order: {}", order.getOrderNumber(), e);
            // Don't throw exception - email failure shouldn't prevent order creation
        }

        // 9. Build response
        return CheckoutResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .trackingToken(order.getTrackingToken())
                .trackingUrl(frontendUrl + "/track/" + order.getTrackingToken())
                .total(order.getTotal())
                .status(order.getStatus())
                .build();
    }

    private String generateOrderNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = orderRepository.count() + 1;
        return String.format("ORD-%s-%04d", date, count);
    }
}
