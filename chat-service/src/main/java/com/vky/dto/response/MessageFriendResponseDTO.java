package com.vky.dto.response;

import lombok.Builder;

@Builder(toBuilder = true)
public record MessageFriendResponseDTO(
        String id,
        String chatRoomId,
        String senderId,
        String recipientId,
        String encryptedMessage,
        String iv,
        String encryptedKeyForRecipient,
        String encryptedKeyForSender,
        String fullDateTime,
        boolean isSuccess,
        int unreadMessageCount) {
}
