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
    TOKEN_ALREADY_USER(5002, "Token Not Found", HttpStatus.CONFLICT);
    private int code;
    private String message;
    HttpStatus httpStatus;
}
