package com.vky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {
    private String id;
    private String chatRoomId;
    private String senderId;
    private String recipientId;
    private String encryptedMessage;
    private String decryptedMessage;
    private String iv;
    private String encryptedKeyForRecipient;
    private String encryptedKeyForSender;
    private Instant fullDateTime;
    private boolean isSeen;

}
