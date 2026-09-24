package com.streamhub.auth.exception;

import org.springframework.http.HttpStatus;

public class GoneException extends ApiException  {
    public GoneException(String message) {
        super(message, HttpStatus.GONE);
    }
}
