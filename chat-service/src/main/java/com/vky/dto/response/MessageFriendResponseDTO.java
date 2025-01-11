package com.vky.dto.response;

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
    private String messageContent;
    private String fullDateTime;
    private boolean isSuccess;
    private int unreadMessageCount;
}
