package com.vky.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TypingMessage {
    private String userId;
    private String chatRoomId;
    private boolean isTyping;
}
