package com.vky.dto.request;

import lombok.Builder;

@Builder(toBuilder = true)
public record UnreadMessageCountDTO(
        String chatRoomId,
        String recipientId,
        String senderId,
        int unreadMessageCount) {
}
