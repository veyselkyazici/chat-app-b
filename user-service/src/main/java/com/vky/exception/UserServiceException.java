package com.vky.exception;

import lombok.Getter;

@Getter
public class UserServiceException extends RuntimeException{
    /**
     * Uygujlama içinde fırlatılacak olan özelleştirilmiş hatalar için kullanılacaktır.
     */
    private final ErrorType errorType;

    public UserServiceException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }

    public UserServiceException(ErrorType errorType, String customMessage){
        super(customMessage);
        this.errorType = errorType;
    }

}
