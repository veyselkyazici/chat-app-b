package com.vky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WsEvent<T> {

    private String type;
    private String targetUserId;
    private T payload;

    public static <T> WsEvent<T> of(String type, String targetUserId, T payload) {
        return WsEvent.<T>builder()
                .type(type)
                .targetUserId(targetUserId)
                .payload(payload)
                .build();
    }
}

