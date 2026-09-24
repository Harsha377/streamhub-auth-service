package com.streamhub.auth.security;

import lombok.Builder;

import java.util.UUID;

/**
 * Represents the authenticated user's information
 * extracted from the Access Token.
 * <p>
 * This object is created after the JWT has been
 * successfully validated.
 * <p>
 * It prevents parsing the JWT multiple times.
 */
@Builder
public record JwtPrincipal(
        Long userId,
        String email,
        UUID sessionId ) {
}