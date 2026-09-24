package com.streamhub.auth.security;

import com.streamhub.auth.entity.User;
import com.streamhub.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Custom implementation of Spring Security's UserDetailsService.
 * <p>
 * Responsibilities:
 * -----------------
 * 1. Load a user from the database.
 * 2. Convert the User entity into CustomUserDetails.
 * 3. Return UserDetails to Spring Security.
 * <p>
 * Spring Security calls this service whenever it needs to
 * authenticate or retrieve the currently authenticated user.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    /**
     * Loads a user by email.
     * <p>
     * In Spring Security, the method name is fixed
     * (loadUserByUsername), but "username" can be any
     * unique identifier.
     * <p>
     * Our application uses email as the username.
     */
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String email, UUID sessionId) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with email: " + email
                        )
                );

        return new CustomUserDetails(user,sessionId);
    }

    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        return loadUserByUsername(email, null);
    }
}
