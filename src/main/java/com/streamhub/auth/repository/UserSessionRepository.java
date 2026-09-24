package com.streamhub.auth.repository;

import com.streamhub.auth.entity.User;
import com.streamhub.auth.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findBySessionId(UUID sessionId);

    Optional<UserSession> findByRefreshTokenHash(String refreshTokenHash);

    List<UserSession> findByUserAndRevokedFalse(User user);

    List<UserSession> findByExpiresAtBefore(Instant instant);

    void deleteByExpiresAtBefore(Instant instant);

    Optional<UserSession> findBySessionIdAndRevokedFalse(
            UUID sessionId
    );

    long countByExpiresAtBefore(Instant now);

    /**
     * Revoke a single active session that belongs to the user.
     *
     * @return number of rows updated.
     */
    @Modifying
    @Transactional
    @Query("""
            UPDATE UserSession us
               SET us.revoked = true
             WHERE us.sessionId = :sessionId
               AND us.user = :user
               AND us.revoked = false
            """)
    int revokeSession(
            @Param("sessionId") UUID sessionId,
            @Param("user") User user
    );

    /**
     * Revokes all active sessions for the specified user.
     *
     * @return Number of sessions revoked.
     */
    @Modifying
    @Transactional
    @Query("""
            UPDATE UserSession us
               SET us.revoked = true
             WHERE us.user = :user
               AND us.revoked = false
            """)
    int revokeAllSessions(
            @Param("user") User user
    );

    /**
     * Revokes all active sessions except the current session.
     */
    @Modifying
    @Transactional
    @Query("""
            UPDATE UserSession us
               SET us.revoked = true
             WHERE us.user = :user
               AND us.sessionId <> :currentSessionId
               AND us.revoked = false
            """)
    void revokeOtherSessions(
            @Param("user") User user,
            @Param("currentSessionId") UUID currentSessionId
    );

    @Modifying
    @Transactional
    @Query("""
                UPDATE UserSession us
                   SET us.lastActivityAt = :lastActivityAt
                 WHERE us.sessionId = :sessionId
                   AND us.lastActivityAt < :threshold
            """)
    int updateLastActivity(
            @Param("sessionId") UUID sessionId,
            @Param("lastActivityAt") Instant lastActivityAt,
            @Param("threshold") Instant threshold
    );
}
