package example.ecommerce.tuyenlm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import example.ecommerce.tuyenlm.entity.InventoryReservation;
import example.ecommerce.tuyenlm.enums.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {

        List<InventoryReservation> findByVariantId(Long variantId);

        List<InventoryReservation> findByVariantIdAndStatus(Long variantId, ReservationStatus status);

        List<InventoryReservation> findByOrderId(Long orderId);

        // Calculate total reserved quantity for a variant
        @Query("SELECT COALESCE(SUM(r.quantity), 0) FROM InventoryReservation r " +
                        "WHERE r.variant.id = :variantId AND r.status = :status")
        int sumQuantityByVariantAndStatus(@Param("variantId") Long variantId,
                        @Param("status") ReservationStatus status);

        // Find expired reservations
        @Query("SELECT r FROM InventoryReservation r " +
                        "WHERE r.expiresAt < :now AND r.status = 'ACTIVE'")
        List<InventoryReservation> findExpiredReservations(@Param("now") LocalDateTime now);

        // Expire reservations (for scheduled job)
        @Modifying
        @Query("UPDATE InventoryReservation r " +
                        "SET r.status = 'EXPIRED' " +
                        "WHERE r.expiresAt < :now AND r.status = 'ACTIVE'")
        int expireReservations(@Param("now") LocalDateTime now);

        // Find active reservations by variant
        @Query("SELECT r FROM InventoryReservation r " +
                        "WHERE r.variant.id = :variantId AND r.status = 'ACTIVE'")
        List<InventoryReservation> findActiveByVariantId(@Param("variantId") Long variantId);
}
