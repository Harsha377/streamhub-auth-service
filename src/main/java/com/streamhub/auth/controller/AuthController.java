package com.streamhub.auth.controller;

import com.streamhub.auth.dto.request.RegisterRequest;
import com.streamhub.auth.dto.request.VerifyOtpRequest;
import com.streamhub.auth.dto.response.RegisterResponse;
import com.streamhub.auth.dto.response.SessionResponse;
import com.streamhub.auth.dto.response.VerifyOtpResponse;
import com.streamhub.auth.security.AuthenticationService;
import com.streamhub.auth.security.CustomUserDetails;
import com.streamhub.auth.service.OtpService;
import com.streamhub.auth.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final OtpService otpService;
    private final UserService userService;
    private final AuthenticationService authenticationService;


    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam String email) {

        otpService.sendOtp(email);

        return "OTP Sent Successfully";
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<VerifyOtpResponse> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request,
            HttpServletRequest httpRequest,HttpServletResponse httpResponse) {

        VerifyOtpResponse response =
                otpService.verifyOtp(request, httpRequest, httpResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request,HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){
        RegisterResponse response = userService.register(request,httpServletRequest,httpServletResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            HttpServletRequest request, HttpServletResponse response) {

        authenticationService.refreshAuthentication(request, response);
        return ResponseEntity.ok().build();

    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        authenticationService.logout(request, response);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutFromAllDevices(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        authenticationService.logoutFromAllDevices(request, response);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<SessionResponse>> getActiveSessions(@AuthenticationPrincipal CustomUserDetails currentUser){
        List<SessionResponse> sessions =
                authenticationService.getActiveSessions(currentUser.getUser(),
                                currentUser.getSessionId());
        return ResponseEntity.ok(sessions);

    }
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> logoutSession(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal CustomUserDetails currentUser ) {

        authenticationService.logoutSession(currentUser.getUser(), sessionId);
        return ResponseEntity.noContent().build();

    }

    @DeleteMapping("/sessions")
    public ResponseEntity<Void> logoutAllSessions(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        authenticationService.logoutAllSessions(currentUser.getUser());
        return ResponseEntity.noContent().build();

    }

    @DeleteMapping("/sessions/others")
    public ResponseEntity<Void> logoutOtherSessions(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        authenticationService.logoutOtherSessions(currentUser.getUser(),
                currentUser.getSessionId());

        return ResponseEntity.noContent().build();

    }
}
