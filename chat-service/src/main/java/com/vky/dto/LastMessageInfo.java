package com.vky.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class LastMessageInfo {
    private String chatRoomId;
    private String id;
    private String encryptedMessage;
    private String iv;
    private String encryptedKeyForRecipient;
    private String encryptedKeyForSender;
    private Instant lastMessageTime;
    private String senderId;
    private String recipientId;
    private boolean isSeen;

}
