package com.streamhub.auth.service;

import com.streamhub.auth.dto.request.VerifyOtpRequest;
import com.streamhub.auth.dto.response.AuthenticationResponse;
import com.streamhub.auth.dto.response.VerifyOtpResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface OtpService {
    void sendOtp(String email);
    VerifyOtpResponse verifyOtp(
            VerifyOtpRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    );
}
