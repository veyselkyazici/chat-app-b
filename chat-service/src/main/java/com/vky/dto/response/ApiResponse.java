package com.vky.dto.response;

import com.vky.expcetion.ErrorMessage;
import lombok.Builder;

import java.util.Collections;
import java.util.List;

@Builder(toBuilder = true)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        List<ErrorMessage> errors) {
    public ApiResponse(boolean success, String message, T data) {
        this(success, message, data, Collections.emptyList());
    }

    public ApiResponse(boolean success, String message, List<ErrorMessage> errors) {
        this(success, message, null, errors);
    }
}
