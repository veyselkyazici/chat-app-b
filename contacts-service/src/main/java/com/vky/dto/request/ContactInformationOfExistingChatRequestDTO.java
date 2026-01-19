package com.vky.dto.request;

import lombok.Builder;
import java.util.UUID;

@Builder(toBuilder = true)
public record ContactInformationOfExistingChatRequestDTO(
        UUID userId,
        UUID userContactId) {
}
