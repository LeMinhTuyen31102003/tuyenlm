package example.ecommerce.tuyenlm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import example.ecommerce.tuyenlm.entity.OrderItem;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    List<OrderItem> findByVariantId(Long variantId);

    // Calculate total revenue from order items
    @Query("SELECT COALESCE(SUM(oi.priceSnapshot * oi.quantity), 0) FROM OrderItem oi WHERE oi.order.id = :orderId")
    BigDecimal calculateOrderTotal(@Param("orderId") Long orderId);

    // Count items in an order
    long countByOrderId(Long orderId);
}
