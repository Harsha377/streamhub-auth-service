package com.streamhub.auth.exception;

import org.springframework.http.HttpStatus;

public class UnprocessableEntityException extends ApiException {
    public UnprocessableEntityException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_CONTENT);
    }
}
