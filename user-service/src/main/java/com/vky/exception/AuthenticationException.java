package com.vky.exception;

import lombok.Getter;

@Getter
public class AuthenticationException extends RuntimeException {

    private final ErrorType errorType;

    public AuthenticationException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }

    public AuthenticationException(ErrorType errorType, String customMessage) {
        super(customMessage);
        this.errorType = errorType;
    }

}
