package com.vky.exception;

import lombok.Getter;

@Getter
public class UserNotFoundException extends RuntimeException{
    /**
     * Uygujlama içinde fırlatılacak olan özelleştirilmiş hatalar için kullanılacaktır.
     */
    private final ErrorType errorType;

    public UserNotFoundException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }

    public UserNotFoundException(ErrorType errorType, String customMessage){
        super(customMessage);
        this.errorType = errorType;
    }

}
