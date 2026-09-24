package com.streamhub.auth.dto.response;

import lombok.Builder;

@Builder
public record RegisterResponse(

        Long id,

        String fullName,

        String email,

        String message

) {
}