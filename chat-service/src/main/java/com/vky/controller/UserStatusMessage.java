package com.vky.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserStatusMessage {
    private String userId;
    private String friendId;
    private boolean online;
    private LocalDateTime lastSeen;

}
