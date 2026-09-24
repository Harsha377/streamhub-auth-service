package com.streamhub.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AuthenticationConfig {
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Password encoder used by Spring Security.
     * <p>
     * Although the application currently authenticates
     * users using Email OTP, we still configure a
     * PasswordEncoder because:
     * <p>
     * - It is required by DaoAuthenticationProvider.
     * - Future password authentication can be added
     *   without changing the configuration.
     * - Admin users may authenticate using passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    /**
     * AuthenticationProvider used by Spring Security.
     * <p>
     * Delegates user loading to CustomUserDetailsService
     * and password verification to PasswordEncoder.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    /**
     * Exposes AuthenticationManager as a Spring Bean.
     * <p>
     * Required when authentication is performed using:
     * <p>
     * authenticationManager.authenticate(...)
     * <p>
     * Although it is not currently used in the OTP flow,
     * exposing this bean makes the security configuration
     * production-ready and supports future authentication
     * mechanisms.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration.getAuthenticationManager();

    }
}
