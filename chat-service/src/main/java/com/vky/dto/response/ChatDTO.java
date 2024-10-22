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
public class ChatDTO {
    private String id;
    private String lastMessage;
    private Instant lastMessageTime;
    private String senderId;
    private String recipientId;
}
