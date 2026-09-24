package com.streamhub.auth.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a single active login session.
 *<p>
 * Returned by:
 *<p>
 * GET /api/v1/auth/sessions
 *<p>
 * This DTO allows the client to display all
 * logged-in devices for the authenticated user.
 */
@Builder
public record SessionResponse(
        /**
         * Unique Session Identifier.
         *<p>
         * Used for:
         * - Logout specific device
         * - Identify current session
         */
        UUID sessionId,
        /**
         * Device Name.
         *<p>
         * Examples:
         * Desktop
         * Samsung Galaxy S24 Ultra
         * iPhone
         */
        String deviceName,
        /**
         * Browser Name.
         *
         * Examples:
         * Chrome
         * Firefox
         * Safari
         */
        String browser,
        /**
         * Operating System.
         *
         * Examples:
         * Windows 11
         * Android 15
         * macOS
         */
        String operatingSystem,
        /**
         * Client IP Address.
         */
        String ipAddress,
        /**
         * Login Time.
         */
        Instant loginAt,

        /**
         * Last Activity Time.
         */
        Instant lastActivityAt,

        /**
         * Indicates whether this session
         * is the device currently making
         * the request.
         *<p>
         * This value is NOT stored
         * in the database.
         */
        boolean currentDevice


        ) {
}
