package com.vky.exception;

import lombok.Getter;

@Getter
public class UserManagerException extends RuntimeException{
    /**
     * Uygujlama içinde fırlatılacak olan özelleştirilmiş hatalar için kullanılacaktır.
     */
    private final ErrorType errorType;

    public UserManagerException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }

    public UserManagerException(ErrorType errorType, String customMessage){
        super(customMessage);
        this.errorType = errorType;
    }

}
