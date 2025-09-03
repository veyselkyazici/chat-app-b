package com.vky.exception;

import lombok.Getter;

@Getter
public class ContactsServiceException extends RuntimeException {
    private final ErrorType errorType;

    public ContactsServiceException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }

    public ContactsServiceException(ErrorType errorType, String customMessage){
        super(customMessage);
        this.errorType = errorType;
    }
    public ContactsServiceException(String message) {
        super(message);
        this.errorType = null;
    }
}
