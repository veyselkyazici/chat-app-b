package com.vky.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class LastMessageInfo {
    private String lastMessage;
    private Instant lastMessageTime;
}
