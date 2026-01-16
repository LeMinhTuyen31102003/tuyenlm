package example.ecommerce.tuyenlm.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory_reservations", indexes = {
        @Index(name = "idx_reservation_expires", columnList = "expiresAt,status"),
        @Index(name = "idx_reservation_variant", columnList = "variant_id,status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order; // NULL khi đang reserve, filled khi committed

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private LocalDateTime reservedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt; // reservedAt + 15 minutes

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.ACTIVE;

    @PrePersist
    void prePersist() {
        if (reservedAt == null) {
            reservedAt = LocalDateTime.now();
        }
        if (expiresAt == null) {
            expiresAt = reservedAt.plusMinutes(15); // Default 15 minutes
        }
    }
}
