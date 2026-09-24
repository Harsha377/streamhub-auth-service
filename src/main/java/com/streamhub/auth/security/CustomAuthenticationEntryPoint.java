package com.streamhub.auth.security;

import com.streamhub.auth.exception.ApiErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;

/**
 * Handles authentication failures.
 * <p>
 * Invoked when a request requires authentication but the
 * user is either:
 * <p>
 * - Not authenticated
 * - JWT is missing
 * - JWT is invalid
 * - JWT is expired
 * <p>
 * Returns HTTP 401 instead of Spring Security's default
 * HTML login page.
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        ApiErrorResponse errorResponse=ApiErrorResponse.builder()
                .httpStatus(HttpStatus.UNAUTHORIZED.value())
                .timestamp(Instant.now())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message(authException.getMessage())
                .path(request.getRequestURI())
                .build();
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(),errorResponse);

    }
}
