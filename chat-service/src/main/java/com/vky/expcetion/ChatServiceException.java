package com.vky.expcetion;

import lombok.Getter;


@Getter
public class ChatServiceException extends RuntimeException {
    private final ErrorType errorType;
    public ChatServiceException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }
    public ChatServiceException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }
}
