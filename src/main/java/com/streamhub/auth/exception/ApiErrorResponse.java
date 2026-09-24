package com.streamhub.auth.exception;

import lombok.*;

import java.time.Instant;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiErrorResponse {
    private int httpStatus;
    private String error;
    private String message;
    private String path;
    private Instant timestamp;
    private Map<String,String> errors;

}
