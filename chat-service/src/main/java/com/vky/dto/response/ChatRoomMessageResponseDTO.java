package com.vky.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomMessageResponseDTO {
    private String id;
    private String chatRoomId;
    private String senderId;
    private String recipientId;
    private String messageContent;
    private Instant fullDateTime;
    private boolean isSeen;
}
