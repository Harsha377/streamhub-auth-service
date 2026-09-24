package com.streamhub.auth.security.impl;

import com.streamhub.auth.config.properties.JwtProperties;
import com.streamhub.auth.device.DeviceInfo;
import com.streamhub.auth.device.DeviceInfoService;
import com.streamhub.auth.dto.response.AuthenticationResponse;
import com.streamhub.auth.dto.response.SessionResponse;
import com.streamhub.auth.entity.User;
import com.streamhub.auth.entity.UserSession;
import com.streamhub.auth.exception.ForbiddenException;
import com.streamhub.auth.exception.ResourceNotFoundException;
import com.streamhub.auth.exception.UnauthorizedException;
import com.streamhub.auth.repository.UserSessionRepository;
import com.streamhub.auth.security.AuthenticationService;
import com.streamhub.auth.service.JwtService;
import com.streamhub.auth.util.CookieUtil;
import com.streamhub.auth.util.RefreshTokenGenerator;
import com.streamhub.auth.util.RefreshTokenHashUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserSessionRepository userSessionRepository;
    private final JwtService jwtService;
    private final CookieUtil cookieUtil;
    private final JwtProperties jwtProperties;
    private final DeviceInfoService deviceInfoService;

    private static final Duration ACTIVITY_UPDATE_INTERVAL =
            Duration.ofMinutes(5);

    @Override
    public AuthenticationResponse authenticate(
            User user,
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        UUID sessionId = generateSessionId();
        String accessToken = generateAccessToken(user, sessionId);
        String refreshToken = generateRefreshToken();
        String refreshTokenHash = hashRefreshToken(refreshToken);

        UserSession userSession = createUserSession(user,
                sessionId, refreshTokenHash, request);

        saveUserSession(userSession);

        addAuthenticationCookies(response, accessToken, refreshToken);

        return buildLoginResponse(user);

    }

    @Override
    @Transactional
    public void refreshAuthentication(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        String refreshToken = extractRefreshToken(request);

        String refreshTokenHash = hashRefreshToken(refreshToken);

        UserSession session = loadUserSession(refreshTokenHash);

        validateUserSession(session);

        User user = loadUser(session);

        String newAccessToken = generateAccessToken(
                user,
                session.getSessionId()
        );

        String newRefreshToken = generateRefreshToken();
        String newRefreshTokenHash =
                hashNewRefreshToken(newRefreshToken);

        rotateUserSession(
                session,
                newRefreshTokenHash
        );


        addAuthenticationCookies(
                response,
                newAccessToken,
                newRefreshToken
        );
    }


    /**
     * Generates a unique session identifier for every successful login.
     * <p>
     * Why do we need Session ID?
     * <p>
     * - Identifies a login session.
     * - Embedded inside the Access Token.
     * - Stored in the user_sessions table.
     * - Used to revoke a single device/session without affecting others.
     * <p>
     * Example:
     * 550e8400-e29b-41d4-a716-446655440000
     */
    private UUID generateSessionId() {
        return UUID.randomUUID();
    }

    /**
     * Generates a JWT Access Token for the authenticated user.
     * <p>
     * The token contains:
     * - User ID
     * - Email (Subject)
     * - Session ID
     * - Issued At
     * - Expiration Time
     * <p>
     * The Session ID links this JWT to a specific record
     * in the user_sessions table.
     */
    private String generateAccessToken(User user, UUID sessionId) {

        return jwtService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                sessionId
        );
    }

    /**
     * Generates a cryptographically secure Refresh Token.
     * <p>
     * Unlike the Access Token, this is NOT a JWT.
     * <p>
     * It is a random 256-bit string generated using SecureRandom.
     * <p>
     * The raw token is sent to the client in an HttpOnly cookie.
     * Only its SHA-256 hash is stored in the database.
     */
    private String generateRefreshToken() {

        return RefreshTokenGenerator.generate();
    }

    /**
     * Hashes the Refresh Token using SHA-256 before persisting.
     * <p>
     * The raw Refresh Token is never stored in the database.
     * <p>
     * Browser:
     * Raw Refresh Token
     * <p>
     * Database:
     * SHA-256 Hash
     * <p>
     * During the Refresh API:
     * 1. Client sends raw Refresh Token.
     * 2. Server hashes it again.
     * 3. Hash is compared with the stored hash.
     */
    private String hashRefreshToken(String refreshToken) {
        return RefreshTokenHashUtil.hash(refreshToken);
    }

    /**
     * Creates a UserSession for the authenticated user.
     * <p>
     * Responsibilities:
     * -----------------
     * 1. Associate the session with the authenticated user.
     * 2. Store the generated Session ID.
     * 3. Store the SHA-256 hash of the Refresh Token.
     * 4. Store the client's device information.
     * 5. Store the client's IP address.
     * 6. Calculate the Refresh Token expiry.
     * <p>
     * Device information is extracted from the HTTP request
     * using DeviceInfoService.
     */
    private UserSession createUserSession(
            User user,
            UUID sessionId,
            String refreshTokenHash,
            HttpServletRequest request
    ) {

        Instant now = Instant.now();

        DeviceInfo deviceInfo = deviceInfoService.extract(request);

        return UserSession.builder()
                .user(user)
                .sessionId(sessionId)
                .refreshTokenHash(refreshTokenHash)
                .deviceName(deviceInfo.deviceName())
                .browser(deviceInfo.browser())
                .operatingSystem(deviceInfo.operatingSystem())
                .ipAddress(deviceInfo.ipAddress())
                .expiresAt(now.plusMillis(jwtProperties.getRefreshTokenExpiry()))

                .build();
    }

    /**
     * Persists the authenticated user's session.
     * <p>
     * Why separate this method?
     * <p>
     * Following the Single Responsibility Principle (SRP),
     * this method is responsible only for saving the session.
     * <p>
     * Benefits:
     * - Easier to test
     * - Easier to modify later
     * - Cleaner authenticate() method
     */
    private void saveUserSession(UserSession session) {
        userSessionRepository.save(session);
    }

    /**
     * Adds authentication cookies to the HTTP response.
     * <p>
     * Two HttpOnly cookies are created:
     * <p>
     * 1. ACCESS_TOKEN
     * 2. REFRESH_TOKEN
     * <p>
     * Browser stores these cookies automatically.
     * <p>
     * JavaScript cannot access them because they
     * are marked as HttpOnly.
     */
    private void addAuthenticationCookies(
            HttpServletResponse response,
            String accessToken,
            String refreshToken
    ) {

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookieUtil.createAccessTokenCookie(accessToken).toString()
        );

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookieUtil.createRefreshTokenCookie(refreshToken).toString()
        );
    }

    /**
     * Builds the login response returned to the client.
     * <p>
     * Note:
     * Access Token and Refresh Token are NOT returned
     * in the response body.
     * <p>
     * They are already stored inside HttpOnly cookies.
     */
    private AuthenticationResponse buildLoginResponse(User user) {

        return AuthenticationResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .message("Login successful.")
                .build();
    }

    private String extractRefreshToken(HttpServletRequest request) {

        String refreshToken = cookieUtil.getCookieValue(
                request, "REFRESH_TOKEN");

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("Refresh token is missing.");
        }

        return refreshToken;
    }

    private UserSession loadUserSession(String refreshTokenHash) {

        return userSessionRepository
                .findByRefreshTokenHash(refreshTokenHash)
                .orElseThrow(() ->
                        new UnauthorizedException("Invalid refresh token.")
                );
    }

    private User loadUser(UserSession session) {

        User user = session.getUser();

        if (user == null) {
            throw new UnauthorizedException("User not found.");
        }

        return user;
    }

    private String generateNewAccessToken(
            User user,
            UserSession session
    ) {

        return jwtService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                session.getSessionId()
        );
    }

    private String generateNewRefreshToken() {
        return RefreshTokenGenerator.generate();
    }

    private String hashNewRefreshToken(String refreshToken) {
        return RefreshTokenHashUtil.hash(refreshToken);
    }

    private void rotateUserSession(
            UserSession session,
            String newRefreshTokenHash
    ) {

        Instant now = Instant.now();

        session.setRefreshTokenHash(newRefreshTokenHash);

        session.setLastActivityAt(now);

        session.setExpiresAt(
                now.plusMillis(jwtProperties.getRefreshTokenExpiry())
        );

        userSessionRepository.save(session);

    }

    @Override
    @Transactional
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        String refreshToken = extractRefreshToken(request);

        String refreshTokenHash = hashRefreshToken(refreshToken);
        UserSession session = loadUserSession(refreshTokenHash);

        validateUserSession(session);
        revokeUserSession(session);
        clearAuthenticationCookies(response);

    }

    /**
     * Validates whether the session can still be used.
     * <p>
     * Validation Rules:
     * -----------------
     * 1. Session must not be revoked.
     * 2. Refresh Token must not be expired.
     * <p>
     * If either validation fails, the user must authenticate
     * again using Email OTP.
     * <p>
     * This method is reused by:
     * - Refresh Token API
     * - Logout API
     * - Logout All Devices API
     */
    private void validateUserSession(UserSession session) {

        if (Boolean.TRUE.equals(session.getRevoked())) {
            throw new UnauthorizedException("Session has already been revoked.");
        }

        if (session.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token has expired.");
        }

    }

    /**
     * Revokes the current user session.
     * <p>
     * Why don't we delete the session?
     * <p>
     * Keeping the session record allows us to maintain:
     * - Login history
     * - Logout history
     * - Device history
     * - Security audit logs
     * <p>
     * Only the session state changes.
     */
    private void revokeUserSession(UserSession session) {

        session.setRevoked(true);

        session.setLogoutAt(Instant.now());

        userSessionRepository.save(session);

    }

    /**
     * Removes both authentication cookies from the browser.
     * <p>
     * Setting Max-Age = 0 instructs the browser
     * to immediately remove the cookies.
     */
    private void clearAuthenticationCookies(
            HttpServletResponse response
    ) {

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookieUtil.deleteAccessTokenCookie().toString()
        );

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookieUtil.deleteRefreshTokenCookie().toString()
        );

    }

    @Override
    @Transactional
    public void logoutFromAllDevices(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        // 1. Read Refresh Token Cookie
        String refreshToken = extractRefreshToken(request);

        // 2. Hash Refresh Token
        String refreshTokenHash = hashRefreshToken(refreshToken);

        // 3. Find Current Session
        UserSession currentSession = loadUserSession(refreshTokenHash);

        // 4. Validate Current Session
        validateUserSession(currentSession);

        // 5. Load User
        User user = loadUser(currentSession);

        // 6. Find All Active Sessions
        List<UserSession> activeSessions =
                loadActiveSessions(user);

        // 7. Revoke All Sessions
        revokeAllSessions(activeSessions);

        // 8. Delete Cookies
        clearAuthenticationCookies(response);

    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getActiveSessions(User currentUser, UUID currentSessionId) {
        return userSessionRepository.findByUserAndRevokedFalse(currentUser)
                .stream().map(session-> mapToSessionResponse(session,currentSessionId)).toList();

    }

    @Override
    @Transactional
    public void logoutSession(User currentUser, UUID sessionId) {

        int updatedRows = userSessionRepository.revokeSession(sessionId, currentUser);

        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Session not found.");
        }
    }

    @Override
    @Transactional
    public void logoutAllSessions(User currentUser) {

        int updatedRows =
                userSessionRepository.revokeAllSessions(currentUser);

        if (updatedRows == 0) {
            throw new ResourceNotFoundException(
                    "No active sessions found."
            );
        }

    }

    @Override
    @Transactional
    public void logoutOtherSessions(User currentUser, UUID currentSessionId) {

        userSessionRepository.revokeOtherSessions(currentUser, currentSessionId);

    }

    @Override
    @Transactional
    public void updateLastActivity(UUID sessionId) {

        Instant now = Instant.now();
        Instant threshold = now.minus(ACTIVITY_UPDATE_INTERVAL);
        userSessionRepository.updateLastActivity(sessionId, now, threshold);

    }

    /**
     * Ensures that the authenticated user owns
     * the session being revoked.
     */
    private void validateSessionOwnership(User currentUser,UserSession session){
        if (!session.getUser().getId().equals(currentUser.getId())){
            throw new ForbiddenException(
                    "You are not authorized to revoke this session.");
        }
    }

    private SessionResponse mapToSessionResponse(UserSession session,UUID currentSessionId){
        return SessionResponse.builder()
                .sessionId(session.getSessionId())
                .deviceName(session.getDeviceName())
                .browser(session.getBrowser())
                .operatingSystem(session.getOperatingSystem())
                .ipAddress(session.getIpAddress())
                .loginAt(session.getLoginAt())
                .lastActivityAt(session.getLastActivityAt())
                .currentDevice(session.getSessionId().equals(currentSessionId)).build();
    }

    /**
     * Loads all active sessions belonging to the user.
     * <p>
     * Only sessions with revoked = false are returned.
     */
    private List<UserSession> loadActiveSessions(User user) {

        return userSessionRepository.findByUserAndRevokedFalse(user);

    }

    /**
     * Revokes every active session belonging to the user.
     */
    private void revokeAllSessions(List<UserSession> sessions) {

        Instant now = Instant.now();

        for (UserSession session : sessions) {

            session.setRevoked(true);

            session.setLogoutAt(now);

        }

        userSessionRepository.saveAll(sessions);

    }


}
