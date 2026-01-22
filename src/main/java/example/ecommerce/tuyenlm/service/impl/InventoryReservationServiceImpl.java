package example.ecommerce.tuyenlm.service.impl;

import example.ecommerce.tuyenlm.entity.InventoryReservation;
import example.ecommerce.tuyenlm.entity.Order;
import example.ecommerce.tuyenlm.entity.ProductVariant;
import example.ecommerce.tuyenlm.enums.ReservationStatus;
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
        // TRỪ STOCK NGAY khi tạo reservation
        variant.setStockQuantity(variant.getStockQuantity() - quantity);
        variantRepository.save(variant);
        log.info("Reduced stock for variant {}: -{}, remaining: {}",
                variant.getSku(), quantity, variant.getStockQuantity());

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

            // Stock đã được trừ khi createReservation, không trừ lại nữa
            log.info("Committed reservation: id={}, variant {} (stock already reduced)",
                    reservation.getId(), reservation.getVariant().getSku());
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
            // Hoàn stock cho cả ACTIVE và COMMITTED reservations
            if (reservation.getStatus() == ReservationStatus.ACTIVE ||
                    reservation.getStatus() == ReservationStatus.COMMITTED) {
                // Return stock
                ProductVariant variant = reservation.getVariant();
                variant.setStockQuantity(variant.getStockQuantity() + reservation.getQuantity());
                variantRepository.save(variant);
                log.info("Returned stock for variant {}: +{}, new total: {}",
                        variant.getSku(), reservation.getQuantity(), variant.getStockQuantity());
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

        // Tìm các reservation đã hết hạn
        List<InventoryReservation> expiredReservations = reservationRepository.findExpiredReservations(now);

        if (!expiredReservations.isEmpty()) {
            // Hoàn stock cho các reservation đã hết hạn
            for (InventoryReservation reservation : expiredReservations) {
                ProductVariant variant = reservation.getVariant();
                variant.setStockQuantity(variant.getStockQuantity() + reservation.getQuantity());
                variantRepository.save(variant);

                reservation.setStatus(ReservationStatus.EXPIRED);
                reservationRepository.save(reservation);

                log.info("Auto-expired reservation id={}, returned stock for variant {}: +{}",
                        reservation.getId(), variant.getSku(), reservation.getQuantity());
            }

            log.info("Auto-expired {} reservations at {}", expiredReservations.size(), now);
        }
    }

    // Scheduled job to cleanup old reservations - chạy mỗi ngày lúc 2h sáng
    @Override
    @Scheduled(cron = "0 0 2 * * *") // 2:00 AM mỗi ngày
    @Transactional
    public void cleanupOldReservations() {
        // Xóa các reservation EXPIRED và COMMITTED cũ hơn 30 ngày
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        int deletedCount = reservationRepository.deleteOldReservations(cutoffDate);

        if (deletedCount > 0) {
            log.info("Cleaned up {} old reservations (older than {})", deletedCount, cutoffDate);
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
