package com.vky.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class LastMessageInfo {
    private String id;
    private String lastMessage;
    private Instant lastMessageTime;
    private String senderId;
    private String recipientId;
}
