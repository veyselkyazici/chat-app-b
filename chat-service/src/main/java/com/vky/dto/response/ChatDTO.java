package com.vky.dto.response;

import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record ChatDTO(
        String id,
        List<String> participantIds,
        List<MessageDTO> messages,
        boolean isLastPage) {
}
