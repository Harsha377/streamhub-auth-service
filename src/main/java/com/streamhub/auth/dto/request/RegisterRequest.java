package com.streamhub.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record RegisterRequest(

        @NotBlank(message = "Full Name is required")
        @Size(max =100,message = "Full name must not exceed 100 characters")
        String fullName,
        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        String email,
        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth

) {


}
