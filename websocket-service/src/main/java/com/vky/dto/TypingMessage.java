package com.vky.dto;

import lombok.Builder;

@Builder(toBuilder = true)
public record TypingMessage(
        String userId,
        String friendId,
        String chatRoomId,
        boolean isTyping) {
}
