package com.streamhub.auth.security;

import com.streamhub.auth.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Custom implementation of Spring Security's UserDetails.
 * <p>
 * Spring Security stores an authenticated user's information
 * inside the SecurityContextHolder using an object that
 * implements the UserDetails interface.
 * <p>
 * This class acts as a bridge between our User entity and
 * Spring Security.
 * <p>
 * Responsibilities:
 * -----------------
 * 1. Expose authenticated user's information.
 * 2. Provide authorities (roles/permissions).
 * 3. Indicate whether the account is valid.
 * <p>
 * NOTE:
 * -----
 * Our application authenticates users using Email OTP.
 * Therefore, password authentication is NOT used.
 * <p>
 * getPassword() returns null because no password is required.
 */
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    /**
     * Our application's User entity.
     */
    private final User user;
    private final UUID sessionId;


    /**
     * Returns the authenticated user's authorities.
     * <p>
     * Currently every authenticated user receives ROLE_USER.
     * <p>
     * Later we can load roles from the database:
     * <p>
     * ROLE_ADMIN
     * ROLE_USER
     * ROLE_MANAGER
     * etc.
     */
    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    /**
     * Password authentication is not used.
     * <p>
     * Since authentication happens using Email OTP,
     * there is no password to expose.
     */
    @Override
    public @Nullable String getPassword() {
        return "";
    }

    /**
     * Spring Security treats this value as the username.
     * <p>
     * We use email as the unique identifier.
     */
    @Override
    @NonNull
    public String getUsername() {
        return user.getEmail();
    }

    /**
     * Indicates whether the account has expired.
     * <p>
     * Returning true means the account is active.
     * <p>
     * Later this can check:
     * - Subscription expiry
     * - Account expiry
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the account is locked.
     * <p>
     * Later this can return false for:
     * - Too many failed login attempts
     * - Suspicious activity
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether credentials have expired.
     * <p>
     * Since we use OTP authentication,
     * credentials never expire.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user account is enabled.
     * <p>
     * Later this can check:
     * - ACTIVE
     * - BLOCKED
     * - SUSPENDED
     * - PENDING
     * <p>
     * Example:
     * <p>
     * return user.getStatus() == AccountStatus.ACTIVE;
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
