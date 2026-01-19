package com.vky.dto.response;

import com.vky.repository.entity.enums.VisibilityOption;
import lombok.Builder;
import java.util.UUID;

@Builder(toBuilder = true)
public record PrivacySettingsResponseDTO(
        UUID id,
        VisibilityOption profilePhotoVisibility,
        VisibilityOption lastSeenVisibility,
        VisibilityOption onlineStatusVisibility,
        VisibilityOption aboutVisibility,
        boolean readReceipts) {
}
