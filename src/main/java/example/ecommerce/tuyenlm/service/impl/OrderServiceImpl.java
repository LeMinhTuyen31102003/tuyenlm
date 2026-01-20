package example.ecommerce.tuyenlm.service.impl;

import example.ecommerce.tuyenlm.dto.response.OrderResponse;
import example.ecommerce.tuyenlm.entity.*;
import example.ecommerce.tuyenlm.enums.OrderStatus;
import example.ecommerce.tuyenlm.exception.InvalidOrderStatusTransitionException;
import example.ecommerce.tuyenlm.exception.ResourceNotFoundException;
import example.ecommerce.tuyenlm.mapping.OrderMapper;
import example.ecommerce.tuyenlm.repository.InventoryReservationRepository;
import example.ecommerce.tuyenlm.repository.OrderRepository;
import example.ecommerce.tuyenlm.service.inter.IEmailService;
import example.ecommerce.tuyenlm.service.inter.IInventoryReservationService;
import example.ecommerce.tuyenlm.service.inter.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final InventoryReservationRepository reservationRepository;
    private final IInventoryReservationService reservationService;
    private final IEmailService emailService;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByTrackingToken(String trackingToken) {
        Order order = orderRepository.findByTrackingToken(trackingToken)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "trackingToken", trackingToken));

        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrders(OrderStatus status, String keyword,
            LocalDateTime fromDate, LocalDateTime toDate,
            Pageable pageable) {

        // Start with a base specification (always true)
        Specification<Order> spec = (root, query, cb) -> cb.conjunction();

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("orderNumber")), "%" + keyword.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("customerName")), "%" + keyword.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("customerPhone")), "%" + keyword.toLowerCase() + "%")));
        }

        if (fromDate != null && toDate != null) {
            spec = spec.and((root, query, cb) -> cb.between(root.get("createdAt"), fromDate, toDate));
        }

        Page<Order> orders = orderRepository.findAll(spec, pageable);
        return orders.map(orderMapper::toOrderResponse);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus, String note) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        OrderStatus currentStatus = order.getStatus();

        // Validate status transition
        if (!isValidTransition(currentStatus, newStatus)) {
            throw new InvalidOrderStatusTransitionException(
                    String.format("Invalid status transition: %s -> %s not allowed", currentStatus, newStatus));
        }

        log.info("Updating order {} status: {} -> {}", order.getOrderNumber(), currentStatus, newStatus);

        // Handle status-specific logic
        if (newStatus == OrderStatus.CONFIRMED &&
                (currentStatus == OrderStatus.PENDING || currentStatus == OrderStatus.PAID)) {
            // Commit reservations and reduce stock
            List<InventoryReservation> reservations = reservationRepository.findByOrderId(orderId);
            reservationService.commitReservations(order, reservations);
            log.info("Committed reservations for order {}", order.getOrderNumber());
        }

        if (newStatus == OrderStatus.CANCELLED) {
            // Release reservations and return stock if not yet committed
            List<InventoryReservation> reservations = reservationRepository.findByOrderId(orderId);
            reservationService.releaseReservations(reservations);
            log.info("Released reservations for cancelled order {}", order.getOrderNumber());
        }

        order.setStatus(newStatus);
        order = orderRepository.save(order);

        // Send status update email
        log.info("Attempting to send status update email for order: {} - new status: {}",
                order.getOrderNumber(), newStatus);
        try {
            emailService.sendOrderStatusUpdateEmail(order);
            log.info("Successfully sent status update email for order: {}", order.getOrderNumber());
        } catch (Exception e) {
            log.error("FAILED to send status update email for order: {} - Error: {}",
                    order.getOrderNumber(), e.getMessage(), e);
            // Don't throw exception - email failure shouldn't prevent status update
        }

        return orderMapper.toOrderResponse(order);
    }

    // Helper methods
    private boolean isValidTransition(OrderStatus from, OrderStatus to) {
        Set<OrderStatus> allowedTransitions = switch (from) {
            case PENDING -> EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED);
            case PAID -> EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED);
            case CONFIRMED -> EnumSet.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED);
            case PROCESSING -> EnumSet.of(OrderStatus.SHIPPING, OrderStatus.CANCELLED);
            case SHIPPING -> EnumSet.of(OrderStatus.DELIVERED);
            case DELIVERED, CANCELLED -> EnumSet.noneOf(OrderStatus.class);
        };

        return allowedTransitions.contains(to);
    }
}
