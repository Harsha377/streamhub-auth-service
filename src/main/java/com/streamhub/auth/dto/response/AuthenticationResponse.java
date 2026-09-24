package com.streamhub.auth.dto.response;

import lombok.Builder;

@Builder
public record AuthenticationResponse(

        Long userId,

        String fullName,

        String email,

        String message

) {
}
