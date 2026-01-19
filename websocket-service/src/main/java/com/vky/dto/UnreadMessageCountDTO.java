package com.vky.dto;

import lombok.Builder;

@Builder(toBuilder = true)
public record UnreadMessageCountDTO(
        String chatRoomId,
        String recipientId,
        String senderId,
        int unreadMessageCount) {
}
