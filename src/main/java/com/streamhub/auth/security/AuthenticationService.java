package com.streamhub.auth.security;

import com.streamhub.auth.dto.response.AuthenticationResponse;
import com.streamhub.auth.dto.response.SessionResponse;
import com.streamhub.auth.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.UUID;

public interface AuthenticationService {

    /**
     * Performs user authentication after OTP verification.
     * <p>
     * Responsibilities (implemented in later phases):
     * <p>
     * 1. Find user
     * 2. Generate Session ID
     * 3. Generate Access Token
     * 4. Generate Refresh Token
     * 5. Hash Refresh Token
     * 6. Save User Session
     * 7. Create HttpOnly Cookies
     * 8. Return Login Response
     */
    AuthenticationResponse authenticate(
            User user,
            HttpServletRequest request,
            HttpServletResponse response
    );

    void refreshAuthentication(
            HttpServletRequest request,
            HttpServletResponse response
    );
    void logout(HttpServletRequest request, HttpServletResponse response);
    void logoutFromAllDevices(
            HttpServletRequest request,
            HttpServletResponse response
    );

    /**
     * Returns all active sessions for the authenticated user.
     *
     * The current device is identified by comparing the
     * authenticated session ID with each stored session.
     *
     * @param currentUser Authenticated user.
     * @param currentSessionId Session ID extracted from JWT.
     * @return Active login sessions.
     */
    List<SessionResponse> getActiveSessions(User currentUser, UUID currentSessionId);

    /**
     * Logs out a specific device by revoking its session.
     *
     * Only the owner of the session is allowed
     * to revoke it.
     *
     * @param currentUser Authenticated user.
     * @param sessionId Session to revoke.
     */
    void logoutSession(
            User currentUser,
            UUID sessionId
    );

    /**
     * Logs out the authenticated user from all devices.
     *
     * All active sessions belonging to the user
     * will be revoked.
     */
    void logoutAllSessions(User currentUser);

    /**
     * Logs out all devices except the current one.
     */
    void logoutOtherSessions(
            User currentUser,
            UUID currentSessionId
    );

    /**
     * Updates the last activity timestamp of the current session.
     *
     * The update is throttled to avoid excessive database writes.
     */
    void updateLastActivity(UUID sessionId);
}
