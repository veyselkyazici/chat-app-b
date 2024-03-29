package com.vky.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomMessageResponseDTO {
    private String id;
    private String chatRoomId;
    private String senderId;
    private String recipientId;
    private String message;
    private LocalDateTime fullDateTime;
    private LocalTime timeOnly;
    private boolean isSeen;
}
