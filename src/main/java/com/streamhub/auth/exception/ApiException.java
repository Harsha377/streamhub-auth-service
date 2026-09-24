package com.streamhub.auth.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class ApiException extends RuntimeException{
    private final HttpStatus httpStatus;
    private final Map<String,String> errors;

    public ApiException(String message,HttpStatus status) {
        super(message);
        this.httpStatus = status;
        this.errors = null;
    }

    public ApiException(String message,HttpStatus httpStatus,Map<String,String> errors){

        super(message);
        this.httpStatus=httpStatus;
        this.errors=errors;
    }

}
