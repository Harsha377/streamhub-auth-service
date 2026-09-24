package com.streamhub.auth.service.impl;

import com.streamhub.auth.config.properties.OtpProperties;
import com.streamhub.auth.dto.request.VerifyOtpRequest;
import com.streamhub.auth.dto.response.AuthenticationResponse;
import com.streamhub.auth.dto.response.VerifyOtpResponse;
import com.streamhub.auth.entity.User;
import com.streamhub.auth.exception.BadRequestException;
import com.streamhub.auth.exception.GoneException;
import com.streamhub.auth.repository.UserRepository;
import com.streamhub.auth.security.AuthenticationService;
import com.streamhub.auth.service.EmailService;
import com.streamhub.auth.service.OtpService;
import com.streamhub.auth.util.OtpGenerator;
import com.streamhub.auth.util.RedisKeyUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;


@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;

    private final StringRedisTemplate stringRedisTemplate;
    private final EmailService emailService;
    private final OtpProperties otpProperties;
    private static final String DEFAULT_NAME = "User";
    @Override
    public void sendOtp(String email) {
        log.info("Generating OTP for {}", email);
        String otp=OtpGenerator.generateOtp(otpProperties.getLength());
        log.debug("OTP stored in Redis with expiry {} minutes", otpProperties.getExpiryMinutes());
       String redisKey= RedisKeyUtil.otpKey(email);
       stringRedisTemplate.opsForValue().set(redisKey,otp,Duration.ofMinutes(otpProperties.getExpiryMinutes()));
       emailService.sendOtpEmail(email,DEFAULT_NAME,otp);


    }

    @Override
    public VerifyOtpResponse verifyOtp(
            VerifyOtpRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {

        String otpKey = RedisKeyUtil.otpKey(request.email());

        // Retrieve OTP from Redis
        String storedOtp = stringRedisTemplate.opsForValue().get(otpKey);

        // OTP expired
        if (storedOtp == null) {
            throw new GoneException("OTP has expired.");
        }

        // Invalid OTP
        if (!storedOtp.equals(request.otp())) {
            throw new BadRequestException("Invalid OTP.");
        }

        // OTP is single use
        Boolean deleted = stringRedisTemplate.delete(otpKey);

        if (Boolean.FALSE.equals(deleted)) {
            log.warn("OTP key already removed for {}", request.email());
        }

        // Check whether user already exists
        User user = userRepository.findByEmail(request.email())
                .orElse(null);

        /*
         * New User
         */
        if (user == null) {

            stringRedisTemplate.opsForValue().set(
                    RedisKeyUtil.verifiedUserKey(request.email()),
                    "VERIFIED",
                    Duration.ofMinutes(10)
            );

            log.info("OTP verified successfully for new user {}", request.email());

            return VerifyOtpResponse.builder()
                    .otpVerified(true)
                    .registered(false)
                    .authenticated(false)
                    .userId(null)
                    .fullName(null)
                    .email(request.email())
                    .message("OTP verified successfully. Please complete registration.")
                    .build();
        }

        /*
         * Existing User
         */
        AuthenticationResponse authenticationResponse =
                authenticationService.authenticate(
                        user,
                        httpRequest,
                        httpResponse
                );

        log.info("User authenticated successfully {}", request.email());

        return VerifyOtpResponse.builder()
                .otpVerified(true)
                .registered(true)
                .authenticated(true)
                .userId(authenticationResponse.userId())
                .fullName(authenticationResponse.fullName())
                .email(authenticationResponse.email())
                .message(authenticationResponse.message())
                .build();
    }


}
