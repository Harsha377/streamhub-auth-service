package com.streamhub.auth.security;

import com.streamhub.auth.entity.UserSession;
import com.streamhub.auth.repository.UserSessionRepository;
import com.streamhub.auth.service.JwtService;
import com.streamhub.auth.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CookieUtil cookieUtil;
    private final UserSessionRepository userSessionRepository;
    private final AuthenticationService authenticationService;

    private final CustomUserDetailsService customUserDetailsService;
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        /*
         * Skip authentication if another filter has already
         * authenticated the current request.
         */
        if (hasAuthentication()) {
            filterChain.doFilter(request, response);
            return;
        }

        /*
         * Read Access Token from HttpOnly cookie.
         */
        String accessToken = extractAccessToken(request);

        /*
         * No Access Token means this request is anonymous.
         * Continue the filter chain.
         */
        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        /*
         * Validate JWT and extract authenticated user information.
         */
        JwtPrincipal principal = validateAndParseToken(accessToken);

        if (principal == null) {
            filterChain.doFilter(request, response);
            return;
        }

        /*
         * Validate the user's active session.
         */
        UserSession session = validateSession(principal);

        if (session == null) {
            filterChain.doFilter(request, response);
            return;
        }
        /*
         * Update session activity.
         *
         * The service throttles updates internally,
         * so this does not result in a database write
         * for every request.
         */
        authenticationService.updateLastActivity(session.getSessionId());

        /*
         * Load user details.
         */
        CustomUserDetails userDetails = loadUser(session);

        /*
         * Build Spring Security Authentication object.
         */
        UsernamePasswordAuthenticationToken authentication =
                createAuthentication(
                        userDetails,
                        request
                );

        /*
         * Store Authentication into SecurityContextHolder.
         */
        storeAuthentication(authentication);

        /*
         * Continue processing the request.
         */
        filterChain.doFilter(request, response);

    }

    /**
     * Checks whether the current request has already been authenticated.
     * <p>
     * Why?
     * ----
     * Multiple security filters may participate in processing the same request.
     * <p>
     * If another filter has already authenticated the user and stored the
     * Authentication object inside the SecurityContext, there is no need
     * to authenticate again.
     * <p>
     * Returning true allows the filter to skip all JWT processing,
     * improving performance and avoiding duplicate authentication.
     * <p>
     * Flow:
     * <p>
     * Request
     *      │
     *      ▼
     * SecurityContextHolder
     *      │
     *      ▼
     * Authentication Present?
     *      │
     *   ┌──┴──┐
     *   │     │
     *  Yes    No
     *   │     │
     *   ▼     ▼
     * Skip   Continue JWT Authentication
     */
    private boolean hasAuthentication() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return authentication != null && authentication.isAuthenticated();

    }

    /**
     * Extracts the Access Token from the HttpOnly cookie.
     * <p>
     * Why?
     * ----
     * Our application stores the JWT inside an HttpOnly cookie
     * instead of the Authorization header.
     * <p>
     * Browser
     *      │
     *      ▼
     * ACCESS_TOKEN Cookie
     *      │
     *      ▼
     * JwtAuthenticationFilter
     * <p>
     * If the cookie does not exist, the request is treated as
     * unauthenticated and the filter simply continues.
     * <p>
     * We DO NOT throw an exception here because:
     * <p>
     * - Public APIs don't require authentication.
     * - Spring Security will later decide whether the requested
     *   endpoint requires authentication.
     */
    private String extractAccessToken(HttpServletRequest request) {

        return cookieUtil.getCookieValue(
                request,
                "ACCESS_TOKEN"
        );

    }
    /**
     * Validates the Access Token and extracts
     * authenticated user information.
     * <p>
     * Validation includes:
     * <p>
     * - JWT Signature
     * - Expiration Time
     * <p>
     * After successful validation, the JWT is parsed
     * only once and converted into JwtPrincipal.
     * <p>
     * JwtPrincipal contains:
     * <p>
     * - User ID
     * - Email
     * - Session ID
     * <p>
     * If validation fails, null is returned and the
     * request continues without authentication.
     */
    private JwtPrincipal validateAndParseToken(
            String accessToken
    ) {

        if (accessToken == null || accessToken.isBlank()) {
            return null;
        }

        if (!jwtService.validateAccessToken(accessToken)) {
            return null;
        }

        return jwtService.parseAccessToken(accessToken);

    }

    /**
     * Validates that the session referenced by the Access Token
     * still exists and has not been revoked.
     * <p>
     * Why?
     * ----
     * A JWT may still be cryptographically valid even after
     * the user has logged out.
     * <p>
     * Therefore, we verify that the corresponding session
     * is still active in the database.
     * <p>
     * Validation:
     * 1. Session exists.
     * 2. Session is not revoked.
     * 3. Refresh token has not expired.
     */
    private UserSession validateSession(JwtPrincipal principal) {

        UserSession session = userSessionRepository
                .findBySessionIdAndRevokedFalse(principal.sessionId())
                .orElse(null);

        if (session == null) {
            return null;
        }

        if (session.getExpiresAt().isBefore(Instant.now())) {
            return null;
        }

        return session;
    }

    /**
     * Loads the authenticated user.
     * <p>
     * Although UserSession already contains a reference
     * to the User entity, we delegate user loading to
     * CustomUserDetailsService.
     * <p>
     * This keeps Spring Security independent from JPA
     * entities and centralizes all user loading logic.
     */
    private CustomUserDetails  loadUser(UserSession session) {

        return (CustomUserDetails) customUserDetailsService
                .loadUserByUsername(
                        session.getUser().getEmail(),
                        session.getSessionId()
                );

    }

    /**
     * Creates Spring Security's Authentication object.
     * <p>
     * This object represents the authenticated user
     * for the current HTTP request.
     * <p>
     * It contains:
     * <p>
     * - UserDetails
     * - Authorities
     * - Authentication state
     */
    private UsernamePasswordAuthenticationToken createAuthentication(
            CustomUserDetails  userDetails,
            HttpServletRequest request
    ) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource()
                        .buildDetails(request)
        );

        return authentication;

    }

    /**
     * Stores the authenticated user inside Spring Security's
     * SecurityContext.
     * <p>
     * Why?
     * ----
     * The SecurityContextHolder is Spring Security's central
     * storage for the currently authenticated user.
     * <p>
     * Once stored, every controller, service and security
     * annotation can access the authenticated user.
     * <p>
     * Example:
     *
     * @AuthenticationPrincipal
     *
     * SecurityContextHolder.getContext().getAuthentication()
     */
    private void storeAuthentication(
            UsernamePasswordAuthenticationToken authentication
    ) {

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

    }
}

//Complete Flow
/**
 * Incoming Request
 *         │
 *         ▼
 * Already Authenticated?
 *         │
 *    ┌────┴─────┐
 *    │          │
 *   Yes        No
 *    │          │
 *    ▼          ▼
 * Continue   Read Access Cookie
 *                 │
 *                 ▼
 *           Cookie Exists?
 *                 │
 *            ┌────┴────┐
 *            │         │
 *           No        Yes
 *            │         │
 *            ▼         ▼
 *       Continue   Validate JWT
 *                       │
 *                       ▼
 *                JWT Valid?
 *                  │
 *             ┌────┴────┐
 *             │         │
 *            No        Yes
 *             │         │
 *             ▼         ▼
 *        Continue  Validate Session
 *                       │
 *                       ▼
 *               Session Valid?
 *                  │
 *             ┌────┴────┐
 *             │         │
 *            No        Yes
 *             │         │
 *             ▼         ▼
 *        Continue   Load User
 *                       │
 *                       ▼
 *            Create Authentication
 *                       │
 *                       ▼
 *          SecurityContextHolder
 *                       │
 *                       ▼
 *           Continue Filter Chain
 * <p>
 *           It only authenticates the current request.
 */