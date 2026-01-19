package example.ecommerce.tuyenlm.service.impl;

import example.ecommerce.tuyenlm.entity.InventoryReservation;
import example.ecommerce.tuyenlm.entity.Order;
import example.ecommerce.tuyenlm.entity.ProductVariant;
import example.ecommerce.tuyenlm.entity.ReservationStatus;
import example.ecommerce.tuyenlm.repository.InventoryReservationRepository;
import example.ecommerce.tuyenlm.repository.ProductVariantRepository;
import example.ecommerce.tuyenlm.service.inter.IInventoryReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryReservationServiceImpl implements IInventoryReservationService {

    private final InventoryReservationRepository reservationRepository;
    private final ProductVariantRepository variantRepository;

    @Override
    @Transactional
    public InventoryReservation createReservation(ProductVariant variant, int quantity, int expirationMinutes) {
        InventoryReservation reservation = new InventoryReservation();
        reservation.setVariant(variant);
        reservation.setQuantity(quantity);
        reservation.setReservedAt(LocalDateTime.now());
        reservation.setExpiresAt(LocalDateTime.now().plusMinutes(expirationMinutes));
        reservation.setStatus(ReservationStatus.ACTIVE);

        reservation = reservationRepository.save(reservation);
        log.info("Created inventory reservation: id={}, variantId={}, quantity={}, expiresAt={}",
                reservation.getId(), variant.getId(), quantity, reservation.getExpiresAt());

        return reservation;
    }

    @Override
    @Transactional
    public void commitReservations(Order order, List<InventoryReservation> reservations) {
        log.info("Committing {} reservations for order {}", reservations.size(), order.getOrderNumber());

        for (InventoryReservation reservation : reservations) {
            reservation.setOrder(order);
            reservation.setStatus(ReservationStatus.COMMITTED);
            reservationRepository.save(reservation);

            // Reduce stock quantity
            ProductVariant variant = reservation.getVariant();
            variant.setStockQuantity(variant.getStockQuantity() - reservation.getQuantity());
            variantRepository.save(variant);

            log.info("Committed reservation: id={}, reduced stock for variant {} by {}",
                    reservation.getId(), variant.getSku(), reservation.getQuantity());
        }
    }

    @Override
    @Transactional
    public void expireReservations(List<InventoryReservation> reservations) {
        for (InventoryReservation reservation : reservations) {
            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);
            log.info("Expired reservation: id={}, variantId={}",
                    reservation.getId(), reservation.getVariant().getId());
        }
    }

    @Override
    @Transactional
    public void releaseReservations(List<InventoryReservation> reservations) {
        log.info("Releasing {} reservations", reservations.size());

        for (InventoryReservation reservation : reservations) {
            if (reservation.getStatus() == ReservationStatus.COMMITTED) {
                // Return stock
                ProductVariant variant = reservation.getVariant();
                variant.setStockQuantity(variant.getStockQuantity() + reservation.getQuantity());
                variantRepository.save(variant);
                log.info("Returned stock for variant {}: +{}", variant.getSku(), reservation.getQuantity());
            }

            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);
        }
    }

    // Scheduled job to auto-expire reservations
    @Override
    @Scheduled(cron = "0 * * * * *") // Every minute
    @Transactional
    public void autoExpireReservations() {
        LocalDateTime now = LocalDateTime.now();
        int expiredCount = reservationRepository.expireReservations(now);

        if (expiredCount > 0) {
            log.info("Auto-expired {} reservations at {}", expiredCount, now);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int getAvailableStock(Long variantId) {
        ProductVariant variant = variantRepository.findById(variantId).orElse(null);
        if (variant == null) {
            return 0;
        }

        int reservedStock = reservationRepository.sumQuantityByVariantAndStatus(
                variantId, ReservationStatus.ACTIVE);

        return Math.max(0, variant.getStockQuantity() - reservedStock);
    }
}
