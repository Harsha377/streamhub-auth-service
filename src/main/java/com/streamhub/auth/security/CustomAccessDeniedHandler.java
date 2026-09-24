package com.streamhub.auth.security;


import com.streamhub.auth.exception.ApiErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;

/**
 * Handles authorization failures.
 * <p>
 * This handler is invoked when:
 * - The user is successfully authenticated.
 * - The user does not have sufficient permissions
 *   to access the requested resource.
 * <p>
 * Example:
 * <p>
 * ROLE_USER
 *      │
 *      ▼
 * Access /api/v1/admin/**
 *      │
 *      ▼
 * HTTP 403 Forbidden
 * <p>
 * This is different from AuthenticationEntryPoint,
 * which handles unauthenticated requests (HTTP 401).
 */
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        ApiErrorResponse errorResponse=ApiErrorResponse.builder()
                .httpStatus(HttpStatus.FORBIDDEN.value())
                .timestamp(Instant.now())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message(accessDeniedException.getMessage())
                .path(request.getRequestURI())
                .build();
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(),errorResponse);
    }
}
