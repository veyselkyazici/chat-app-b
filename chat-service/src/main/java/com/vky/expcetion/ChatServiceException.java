package com.vky.expcetion;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


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
