package com.vky.dto.request;

import lombok.Builder;

@Builder(toBuilder = true)
public record CreateChatRoom(
        String userId,
        String friendId) {
}
