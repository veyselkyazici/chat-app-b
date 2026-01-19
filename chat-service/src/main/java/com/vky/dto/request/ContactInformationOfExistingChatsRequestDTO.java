package com.vky.dto.request;

import lombok.Builder;
import java.util.List;
import java.util.UUID;

@Builder(toBuilder = true)
public record ContactInformationOfExistingChatsRequestDTO(
        UUID userId,
        List<UUID> userContactIds) {
}
