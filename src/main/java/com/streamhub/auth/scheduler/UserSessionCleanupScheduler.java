package com.streamhub.auth.scheduler;

import com.streamhub.auth.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Periodically removes expired user sessions.
 * <p>
 * Why?
 * ----
 * When a refresh token expires, its corresponding
 * UserSession is no longer useful.
 * <p>
 * Keeping expired sessions forever would:
 * <p>
 * - Increase database size
 * - Slow down queries
 * - Waste storage
 * <p>
 * Therefore, this scheduler periodically deletes
 * expired sessions from the database.
 * <p>
 * Current Strategy
 * ----------------
 * Every hour:
 * <p>
 * DELETE
 * FROM user_sessions
 * WHERE expires_at < CURRENT_TIMESTAMP;
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserSessionCleanupScheduler {
    private final UserSessionRepository userSessionRepository;

    @Transactional
    @Scheduled(cron = "${session.cleanup.cron}")
    public void cleanupExpiredSessions(){
        Instant now=Instant.now();
        long expiredSessions=userSessionRepository.countByExpiresAtBefore(now);
        if (expiredSessions==0){
            log.debug("No expired user sessions found.");
            return;
        }
        userSessionRepository.deleteByExpiresAtBefore(now);
        log.info("Deleted {} expired user sessions.", expiredSessions);
    }
}

/**
 * Spring Scheduler
 *         │
 *         ▼
 * UserSessionCleanupScheduler
 *         │
 *         ▼
 * countByExpiresAtBefore(now)
 *         │
 *         ▼
 * Expired Sessions?
 *       │
 *  ┌────┴─────┐
 *  │          │
 * No         Yes
 *  │          │
 *  ▼          ▼
 * Return   deleteByExpiresAtBefore(now)
 *               │
 *               ▼
 *          PostgreSQL
 */