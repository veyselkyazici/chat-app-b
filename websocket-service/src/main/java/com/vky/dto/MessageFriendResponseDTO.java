package com.vky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageFriendResponseDTO {
    private String id;
    private String chatRoomId;
    private String senderId;
    private String recipientId;
    private String encryptedMessage;
    private String iv;
    private String encryptedKeyForRecipient;
    private String encryptedKeyForSender;
    private String fullDateTime;
    private boolean isSuccess;
    private int unreadMessageCount;
}
