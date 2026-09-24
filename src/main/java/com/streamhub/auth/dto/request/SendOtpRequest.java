package com.streamhub.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendOtpRequest(

        @Email
        @NotBlank(message = "Invalid email address")
        String email

) {
}
