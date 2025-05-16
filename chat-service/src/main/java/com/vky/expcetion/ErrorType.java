package com.vky.expcetion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ErrorType {
    INTERNAL_ERROR(2000, "Internal Server Error", INTERNAL_SERVER_ERROR),
    BAD_REQUEST_ERROR(2001, "Invalid Parameter Error", BAD_REQUEST),
    AUTHENTICATION_FAILED(1000, "Invalid username or password", UNAUTHORIZED),
    UNEXPECTED_ERROR(2002, "Unexpected error occurred. Please again later", INTERNAL_SERVER_ERROR),
    SENDER_BLOCKED(4001, "You are blocked and cannot send messages to this user", FORBIDDEN),
    RECIPIENT_BLOCKED(4002, "Recipient has blocked you from sending messages", FORBIDDEN);


    private int code;
    private String message;
    HttpStatus httpStatus;
}
