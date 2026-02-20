package com.vky.exception;

import com.vky.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
        @ExceptionHandler(AuthManagerException.class)
        public ResponseEntity<ApiResponse<Void>> handleAuthManagerException(AuthManagerException ex) {
                ErrorType errorType = ex.getErrorType();
                ErrorMessage errorMessage = ErrorMessage.builder()
                                .code(errorType.getCode())
                                .message(errorType.getMessage())
                                .fields(Collections.emptyList())
                                .build();

                return ResponseEntity.status(errorType.getHttpStatus())
                                .body(new ApiResponse<>(false, errorType.getMessage(), List.of(errorMessage)));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
                List<String> invalidFields = ex.getBindingResult().getFieldErrors().stream()
                                .map(FieldError::getField)
                                .distinct()
                                .collect(Collectors.toList());

                ErrorMessage errorMessage = ErrorMessage.builder()
                                .code(ErrorType.VALIDATION_FAILED.getCode())
                                .message("Validation failed")
                                .fields(invalidFields)
                                .build();

                return ResponseEntity.badRequest()
                                .body(new ApiResponse<>(false, "Validation error", List.of(errorMessage)));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
                ErrorMessage errorMessage = ErrorMessage.builder()
                                .code(ErrorType.INTERNAL_SERVER_ERROR.getCode())
                                .message("Unexpected error")
                                .fields(Collections.emptyList())
                                .build();

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new ApiResponse<>(false, "Internal server error", List.of(errorMessage)));
        }

        // @ExceptionHandler(BadCredentialsException.class)
        // public ResponseEntity<ApiResponse<Void>>
        // handleBadCredentialsException(BadCredentialsException ex) {
        // ErrorMessage errorMessage = ErrorMessage.builder()
        // .code(ErrorType.INVALID_CREDENTIALS.getCode())
        // .message("Incorrect email address or password.")
        // .fields(Collections.emptyList())
        // .build();
        //
        // return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        // .body(new ApiResponse<>(false, "Incorrect email address or password.",
        // List.of(errorMessage)));
        // }

}
