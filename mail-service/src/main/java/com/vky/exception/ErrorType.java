package com.vky.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ErrorType {
    TOKEN_NOT_FOUND(5001, "Token Not Found", HttpStatus.NOT_FOUND),
    TOKEN_ALREADY_USER(5002, "Token Not Found", HttpStatus.CONFLICT),
    TOKEN_EXPIRED(5003, "Token Expired", HttpStatus.BAD_REQUEST),
    TOKEN_ALREADY_USED(5004, "Token Already Used" , HttpStatus.CONFLICT ),
    AUTH_ID_MISSING(5005, "Auth Id Missing" , HttpStatus.BAD_REQUEST ),;
    private int code;
    private String message;
    HttpStatus httpStatus;
}
