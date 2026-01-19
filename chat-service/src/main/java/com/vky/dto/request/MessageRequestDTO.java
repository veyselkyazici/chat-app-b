package com.vky.dto.request;

import lombok.Builder;

@Builder(toBuilder = true)
public record MessageRequestDTO(
        String encryptedMessage,
        String iv,
        String encryptedKeyForRecipient,
        String encryptedKeyForSender,
        String senderId,
        String recipientId,
        String fullDateTime,
        String chatRoomId,
        String userChatSettingsId) {
}
