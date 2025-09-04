package com.vky.exception;

import lombok.Getter;

@Getter
public class MailServiceException extends RuntimeException {
    private final ErrorType errorType;
    public MailServiceException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }
    public MailServiceException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }
}
