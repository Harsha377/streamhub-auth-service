package com.streamhub.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "user_sessions",
        indexes = {
                @Index(
                        name = "idx_user_sessions_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_user_sessions_session",
                        columnList = "session_id"
                ),
                @Index(
                        name = "idx_user_sessions_expiry",
                        columnList = "expires_at"
                ),
                @Index(
                        name = "idx_user_sessions_revoked",
                        columnList = "revoked"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Generated inside AuthenticationService.
     * Never changes during the lifetime of the session.
     */
    @Column(
            name = "session_id",
            nullable = false,
            unique = true
    )
    private UUID sessionId;

    /**
     * SHA-256 hash of the refresh token.
     */
    @Column(
            name = "refresh_token_hash",
            nullable = false,
            unique = true,
            length = 255
    )
    private String refreshTokenHash;

    @Column(
            name = "device_name",
            length = 100
    )
    private String deviceName;

    @Column(length = 100)
    private String browser;

    @Column(
            name = "operating_system",
            length = 100
    )
    private String operatingSystem;

    @Column(
            name = "ip_address",
            length = 50
    )
    private String ipAddress;

    /**
     * Login time.
     * Never changes.
     */
    @Column(
            name = "login_at",
            nullable = false
    )
    private Instant loginAt;

    /**
     * Updated every refresh.
     */
    @Column(
            name = "last_activity_at",
            nullable = false
    )
    private Instant lastActivityAt;

    /**
     * Refresh token expiry.
     */
    @Column(
            name = "expires_at",
            nullable = false
    )
    private Instant expiresAt;

    @Builder.Default
    @Column(nullable = false)
    private Boolean revoked = false;

    @Column(name = "logout_at")
    private Instant logoutAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_session_user")
    )
    private User user;

    @PrePersist
    public void prePersist() {

        Instant now = Instant.now();

        if (loginAt == null) {
            loginAt = now;
        }

        if (lastActivityAt == null) {
            lastActivityAt = now;
        }

        if (revoked == null) {
            revoked = false;
        }
    }

}
