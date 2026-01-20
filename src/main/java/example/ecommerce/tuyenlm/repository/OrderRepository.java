package example.ecommerce.tuyenlm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import example.ecommerce.tuyenlm.entity.Order;
import example.ecommerce.tuyenlm.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

        Optional<Order> findByOrderNumber(String orderNumber);

        Optional<Order> findByTrackingToken(String trackingToken);

        Page<Order> findByStatus(OrderStatus status, Pageable pageable);

        Page<Order> findByCustomerEmail(String email, Pageable pageable);

        Page<Order> findByCustomerPhone(String phone, Pageable pageable);

        @Query("SELECT o FROM Order o WHERE o.orderNumber LIKE %:keyword% " +
                        "OR o.customerName LIKE %:keyword% " +
                        "OR o.customerPhone LIKE %:keyword%")
        Page<Order> searchOrders(@Param("keyword") String keyword, Pageable pageable);

        @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :fromDate AND :toDate")
        Page<Order> findByDateRange(@Param("fromDate") LocalDateTime fromDate,
                        @Param("toDate") LocalDateTime toDate,
                        Pageable pageable);

        @Query("SELECT o FROM Order o WHERE o.status = :status " +
                        "AND o.createdAt BETWEEN :fromDate AND :toDate")
        Page<Order> findByStatusAndDateRange(@Param("status") OrderStatus status,
                        @Param("fromDate") LocalDateTime fromDate,
                        @Param("toDate") LocalDateTime toDate,
                        Pageable pageable);

        // Count orders by status
        long countByStatus(OrderStatus status);

        // Find latest order number for generating new order number
        @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
        Page<Order> findLatestOrder(Pageable pageable);

        // Fetch order with items (for email sending)
        @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
        Optional<Order> findByIdWithItems(@Param("id") Long id);
}
