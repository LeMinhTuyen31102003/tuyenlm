package example.ecommerce.tuyenlm.service.inter;

import example.ecommerce.tuyenlm.dto.response.OrderResponse;
import example.ecommerce.tuyenlm.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface IOrderService {
    OrderResponse getOrderByTrackingToken(String trackingToken);

    OrderResponse getOrderById(Long id);

    Page<OrderResponse> getOrders(OrderStatus status, String keyword,
            LocalDateTime fromDate, LocalDateTime toDate,
            Pageable pageable);

    Page<OrderResponse> getOrdersByEmail(String email, Pageable pageable);

    OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus, String note);
}
