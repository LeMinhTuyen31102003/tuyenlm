package example.ecommerce.tuyenlm.config;

import example.ecommerce.tuyenlm.repository.CartRepository;
import example.ecommerce.tuyenlm.repository.InventoryReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final InventoryReservationRepository reservationRepository;
    private final CartRepository cartRepository;

    /**
     * ✅ Tự động expire reservations hết hạn
     * Chạy mỗi 5 phút
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional
    public void expireReservations() {
        LocalDateTime now = LocalDateTime.now();

        int expiredCount = reservationRepository.expireReservations(now);

        if (expiredCount > 0) {
            log.info("⏰ Expired {} inventory reservations", expiredCount);
        }
    }

    /**
     * ✅ Tự động xóa carts cũ không hoạt động sau 7 ngày
     * Chạy mỗi ngày lúc 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupAbandonedCarts() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);

        // Count before delete
        long count = cartRepository.count();

        cartRepository.deleteByUpdatedAtBefore(cutoffDate);

        long deletedCount = count - cartRepository.count();

        if (deletedCount > 0) {
            log.info("🧹 Cleaned up {} abandoned carts older than 7 days", deletedCount);
        }
    }
}
