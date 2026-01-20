package example.ecommerce.tuyenlm.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class TokenBlacklistService {

    // Map: token -> expiry time
    private final Map<String, LocalDateTime> blacklistedTokens = new ConcurrentHashMap<>();

    /**
     * Add token to blacklist
     */
    public void blacklistToken(String token, LocalDateTime expiryTime) {
        blacklistedTokens.put(token, expiryTime);
        log.info("Token added to blacklist. Total blacklisted: {}", blacklistedTokens.size());
    }

    /**
     * Check if token is blacklisted
     */
    public boolean isBlacklisted(String token) {
        return blacklistedTokens.containsKey(token);
    }

    /**
     * Remove expired tokens from blacklist every hour
     * This prevents memory leak
     */
    @Scheduled(fixedRate = 3600000) // Run every 1 hour
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        int beforeSize = blacklistedTokens.size();

        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));

        int afterSize = blacklistedTokens.size();
        int removed = beforeSize - afterSize;

        if (removed > 0) {
            log.info("Cleaned up {} expired tokens from blacklist. Remaining: {}", removed, afterSize);
        }
    }
}
