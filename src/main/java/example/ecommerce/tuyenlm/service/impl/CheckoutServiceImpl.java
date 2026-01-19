package example.ecommerce.tuyenlm.service.impl;

import example.ecommerce.tuyenlm.dto.request.CheckoutRequest;
import example.ecommerce.tuyenlm.dto.response.CheckoutResponse;
import example.ecommerce.tuyenlm.entity.*;
import example.ecommerce.tuyenlm.exception.EmptyCartException;
import example.ecommerce.tuyenlm.exception.InsufficientStockException;
import example.ecommerce.tuyenlm.exception.ResourceNotFoundException;
import example.ecommerce.tuyenlm.repository.*;
import example.ecommerce.tuyenlm.service.inter.ICheckoutService;
import example.ecommerce.tuyenlm.service.inter.IEmailService;
import example.ecommerce.tuyenlm.service.inter.IInventoryReservationService;
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

    @Value("${app.frontend.url:http://localhost:8080}")
    private String frontendUrl;

    private static final int RESERVATION_EXPIRATION_MINUTES = 15;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CheckoutResponse processCheckout(CheckoutRequest request) {
        log.info("Processing checkout for sessionId: {}", request.getSessionId());

        // 1. Find cart
        Cart cart = cartRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "sessionId", request.getSessionId()));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new EmptyCartException("Cannot checkout with empty cart");
        }

        // 2. Validate stock and create reservations
        List<InventoryReservation> reservations = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            // Lock variant to prevent race condition
            ProductVariant variant = variantRepository.findByIdWithLock(cartItem.getVariant().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", cartItem.getVariant().getId()));

            // Calculate available stock
            int reservedStock = reservationRepository.sumQuantityByVariantAndStatus(
                    variant.getId(), ReservationStatus.ACTIVE);
            int availableStock = variant.getStockQuantity() - reservedStock;

            // Validate
            if (availableStock < cartItem.getQuantity()) {
                throw new InsufficientStockException(
                        String.format("Variant '%s' is out of stock", variant.getSku()),
                        Map.of(
                                "variantId", variant.getId(),
                                "sku", variant.getSku(),
                                "requested", cartItem.getQuantity(),
                                "available", availableStock));
            }

            // Create reservation
            InventoryReservation reservation = reservationService.createReservation(
                    variant, cartItem.getQuantity(), RESERVATION_EXPIRATION_MINUTES);
            reservations.add(reservation);

            subtotal = subtotal.add(cartItem.getPriceSnapshot().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        // 3. Create order
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

        // 4. Create order items
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

        // 5. Link reservations to order
        for (InventoryReservation reservation : reservations) {
            reservation.setOrder(order);
            reservationRepository.save(reservation);
        }

        // 6. Clear cart
        cartItemRepository.deleteByCartId(cart.getId());
        log.info("Cleared cart for sessionId: {}", request.getSessionId());

        // 7. Send order confirmation email
        try {
            emailService.sendOrderConfirmationEmail(order);
            log.info("Order confirmation email sent for order: {}", order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email for order: {}", order.getOrderNumber(), e);
            // Don't throw exception - email failure shouldn't prevent order creation
        }

        // 8. Build response
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(RESERVATION_EXPIRATION_MINUTES);

        return CheckoutResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .trackingToken(order.getTrackingToken())
                .trackingUrl(frontendUrl + "/track/" + order.getTrackingToken())
                .total(order.getTotal())
                .status(order.getStatus())
                .reservationExpiresAt(expiresAt)
                .build();
    }

    private String generateOrderNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = orderRepository.count() + 1;
        return String.format("ORD-%s-%04d", date, count);
    }
}
