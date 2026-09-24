package com.streamhub.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    /**
     * Our custom JWT authentication filter.
     * <p>
     * Responsibilities:
     * -----------------
     * - Read Access Token from HttpOnly cookie.
     * - Validate the JWT.
     * - Validate the user session.
     * - Load the authenticated user.
     * - Store Authentication in SecurityContextHolder.
     * <p>
     * This filter executes before Spring Security's
     * UsernamePasswordAuthenticationFilter.
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    /**
     * Spring Security AuthenticationProvider.
     * <p>
     * Responsible for performing authentication.
     * <p>
     * Although our application currently authenticates users
     * using Email OTP, we still register an AuthenticationProvider
     * because Spring Security's infrastructure expects one.
     * <p>
     * In our project, this provider will use:
     * - CustomUserDetailsService
     * - PasswordEncoder (reserved for future password support)
     */
    private final AuthenticationProvider authenticationProvider;
    /**
     * Handles HTTP 401 (Unauthorized).
     */
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    /**
     * Handles HTTP 403 (Forbidden).
     */
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                /*
                 * Disable CSRF because the application
                 * uses JWT based stateless authentication.
                 */
                .csrf(AbstractHttpConfigurer::disable)
                /*
                 * Never create an HTTP Session.
                 * Every request must authenticate
                 * using the Access Token.
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                /*
                 * Register AuthenticationProvider.
                 */
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(authorize -> authorize

                        /*
                         * Public Authentication APIs.
                         */
                        .requestMatchers(

                                HttpMethod.POST, "/api/v1/auth/send-otp",
                                "/api/v1/auth/verify-otp","/api/v1/auth/register",
                                "/api/v1/auth/refresh").permitAll()

                        /*
                         * Requires authenticated user.
                         */
                        .requestMatchers(
                                "/api/v1/auth/logout",
                                "/api/v1/auth/sessions","/api/v1/auth/sessions/{sessionId}",
                                "/api/v1/auth/sessions","/api/v1/auth/sessions/others",
                                "/api/v1/auth/logout-all").authenticated()

                        /*
                         * Swagger (optional)
                         */
                        .requestMatchers(

                                "/swagger-ui/**",
                                "/v3/api-docs/**"

                        ).permitAll()

                        /*
                         * Every remaining API
                         * requires authentication.
                         */
                        .anyRequest().authenticated()
                ).exceptionHandling(exception ->
                        exception.authenticationEntryPoint(authenticationEntryPoint)
                                .accessDeniedHandler(accessDeniedHandler))
                /*
                 * Register our JWT Filter.
                 */
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}



/**
 * Incoming Request
 *         │
 *         ▼
 * JwtAuthenticationFilter
 *         │
 *         ▼
 * SecurityContextHolder
 *         │
 *         ▼
 * authorizeHttpRequests()
 *         │
 *         ├───────────────┐
 *         │               │
 *         ▼               ▼
 * Public URL        Protected URL
 * permitAll()       authenticated()
 *         │               │
 *         ▼               ▼
 *  Controller      Authentication?
 *                       │
 *                ┌──────┴──────┐
 *                │             │
 *               Yes            No
 *                │             │
 *                ▼             ▼
 *         Controller      AuthenticationEntryPoint
 *                               │
 *                               ▼
 *                         HTTP 401 Unauthorized
 */