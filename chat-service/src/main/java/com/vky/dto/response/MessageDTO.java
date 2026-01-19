package com.vky.dto.response;

import lombok.Builder;

import java.time.Instant;

@Builder(toBuilder = true)
public record MessageDTO(
        String id,
        String chatRoomId,
        String senderId,
        String recipientId,
        String encryptedMessage,
        String decryptedMessage,
        String iv,
        String encryptedKeyForRecipient,
        String encryptedKeyForSender,
        Instant fullDateTime,
        boolean isSeen) {
}
