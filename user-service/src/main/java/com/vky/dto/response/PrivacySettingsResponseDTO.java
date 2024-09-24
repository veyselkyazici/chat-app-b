package com.vky.dto.response;

import com.vky.repository.entity.enums.VisibilityOption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PrivacySettingsResponseDTO {
    private UUID id;
    private VisibilityOption profilePhotoVisibility;
    private VisibilityOption lastSeenVisibility ;
    private VisibilityOption onlineStatusVisibility;
    private boolean readReceipts = true;
}
