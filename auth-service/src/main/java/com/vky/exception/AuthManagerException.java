package com.vky.exception;

import lombok.Getter;

@Getter
public class AuthManagerException extends RuntimeException{
    /**
     * Uygujlama içinde fırlatılacak olan özelleştirilmiş hatalar için kullanılacaktır.
     */
    private final ErrorType errorType;

    public AuthManagerException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }

    public AuthManagerException(ErrorType errorType, String customMessage){
        super(customMessage);
        this.errorType = errorType;
    }

}
