package com.vky.dto;

import lombok.Builder;

@Builder(toBuilder = true)
public record LastSeenDTO(
        String lastSeen) {
}
