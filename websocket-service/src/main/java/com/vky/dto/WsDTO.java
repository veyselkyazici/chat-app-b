package com.vky.dto;

public record WsDTO(
        String eventId,
        String type,
        Object data
) {}
