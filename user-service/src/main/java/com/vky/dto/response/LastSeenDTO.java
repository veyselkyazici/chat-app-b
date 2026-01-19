package com.vky.dto.response;

import lombok.Builder;

@Builder(toBuilder = true)
public record LastSeenDTO(
        String lastSeen) {
}
