package com.vky.dto.request;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class UpdateLastSeenRequestDTO {
    private UUID userId;
    private Instant lastSeen;
}
