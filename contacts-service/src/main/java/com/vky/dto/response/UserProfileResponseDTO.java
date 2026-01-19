package com.vky.dto.response;

import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Builder(toBuilder = true)
public record UserProfileResponseDTO(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String imagee,
        String about,
        Instant updatedAt,
        PrivacySettingsResponseDTO privacySettings,
        UserKeyResponseDTO userKey) {
}
