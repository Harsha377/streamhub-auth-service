package com.streamhub.auth.service;

import com.streamhub.auth.security.JwtPrincipal;

import java.util.UUID;

public interface JwtService {

    String generateAccessToken(Long userId, String email, UUID sessionId);
    /**
     * Validates the JWT signature and expiration.
     */
    boolean validateAccessToken(String token);
    Long extractUserId(String token);
    String extractEmail(String token);
    UUID extractSessionId(String token);
    /**
     * Parses the JWT once and returns all required
     * authenticated user information.
     */
    JwtPrincipal parseAccessToken(String token);
}
