package com.vky.dto.response;

import lombok.Builder;

import java.time.Instant;

@Builder(toBuilder = true)
public record UserChatSettingsDTO(
        String id,
        String userId,
        String chatRoomId,
        int unreadMessageCount,
        boolean isArchived,
        boolean isPinned,
        boolean isBlocked,
        boolean isBlockedMe,
        Instant deletedTime,
        Instant blockedTime,
        Instant unblockedTime) {
}
