package com.vky.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserChatSettingsDTO {
    private String id;
    private String userId;
    private String chatRoomId;
    private int unreadMessageCount;
    private boolean isArchived;
    private boolean isPinned;
    private boolean isBlocked;
    private boolean isBlockedMe;
    private Instant deletedTime;
    private Instant blockedTime;
    private Instant unblockedTime;
}
