package com.vky.expcetion;

import com.vky.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {



    private record ErrorDetail(int errorCode, String errorType) { }

    @ExceptionHandler(ChatServiceException.class)
    public ResponseEntity<ApiResponse<?>> handleUserServiceException(ChatServiceException ex) {

        return ResponseEntity.status(ex.getErrorType().getHttpStatus())
                .body(new ApiResponse<>(
                        false,
                        ex.getMessage(),
                        new ErrorDetail(ex.getErrorType().getCode(), ex.getErrorType().name())
                ));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
    }
}