package com.vky.dto.request;

import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Builder(toBuilder = true)
public record UpdateLastSeenRequestDTO(
        UUID userId,
        Instant lastSeen) {
}
