package com.vky.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class UpdateLastSeenRequestDTO {
    private UUID userId;
    private Instant lastSeen;
}
