package com.streamhub.auth.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class BadRequestException extends ApiException {
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
    public BadRequestException(String message, Map<String,String> errors){
        super(message,HttpStatus.BAD_REQUEST,errors);
    }

}
