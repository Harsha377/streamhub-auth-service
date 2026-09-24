package com.streamhub.auth.dto.response;

import lombok.Builder;

@Builder
public record VerifyOtpResponse(

        boolean otpVerified,

        boolean registered,

        boolean authenticated,

        Long userId,

        String fullName,

        String email,

        String message

) {
}
