package com.streamhub.auth.exception;

import org.springframework.http.HttpStatus;

public class RegistrationRequiredException extends ApiException{
    public RegistrationRequiredException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
