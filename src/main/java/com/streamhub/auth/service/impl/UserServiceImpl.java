package com.streamhub.auth.service.impl;

import com.streamhub.auth.dto.request.RegisterRequest;
import com.streamhub.auth.dto.response.RegisterResponse;
import com.streamhub.auth.entity.User;
import com.streamhub.auth.exception.ConflictException;
import com.streamhub.auth.exception.ForbiddenException;
import com.streamhub.auth.mapper.UserMapper;
import com.streamhub.auth.repository.UserRepository;
import com.streamhub.auth.security.AuthenticationService;
import com.streamhub.auth.service.UserService;
import com.streamhub.auth.util.RedisKeyUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final AuthenticationService authenticationService;
    @Override
    public RegisterResponse register(RegisterRequest request, HttpServletRequest httpServletRequest,
                                     HttpServletResponse httpServletResponse) {
        String verification=stringRedisTemplate.opsForValue().get(RedisKeyUtil.verifiedUserKey(request.email()));
        if (verification==null){
            throw new ForbiddenException("Please verify your email before registration.");
        }
        // Check duplicate email
        if (userRepository.existsByEmail(request.email())){
            throw new ConflictException("Email is already registered.");
        }
        // Convert DTO -> Entity
        User user=userMapper.toEntity(request);
        //save user
        User savedUser=userRepository.save(user);
        authenticationService.authenticate(
                savedUser,
                httpServletRequest,
                httpServletResponse
        );
        log.info("User registered successfully with email : {}", savedUser.getEmail());
        stringRedisTemplate.delete(RedisKeyUtil.verifiedUserKey(request.email()));
        // Convert Entity -> Response
        RegisterResponse response = userMapper.toRegisterResponse(savedUser);
        return RegisterResponse.builder()
                .id(response.id())
                .email(response.email())
                .fullName(response.fullName())
                .message("Registration completed successfully")
                .build();
    }
}
