package com.freelanceflow.notification;

import com.freelanceflow.auth.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CleanupSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(CleanupSchedulerService.class);

    private final RefreshTokenRepository refreshTokenRepository;

    public CleanupSchedulerService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    // Run every Sunday at 3 AM IST
    @Scheduled(cron = "0 0 3 * * SUN", zone = "Asia/Kolkata")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting expired refresh token cleanup...");
        try {
            refreshTokenRepository.deleteAllExpired();
            log.info("Successfully cleaned up expired refresh tokens.");
        } catch (Exception e) {
            log.error("Error during expired refresh token cleanup", e);
        }
    }
}
