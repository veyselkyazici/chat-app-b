package com.vky.dto.request;

import com.vky.repository.entity.enums.VisibilityOption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PrivacySettingsRequestDTO {
    private VisibilityOption profilePhotoVisibility;
    private VisibilityOption lastSeenVisibility ;
    private VisibilityOption onlineStatusVisibility;
    private VisibilityOption aboutVisibility;
    private boolean readReceipts = true;
}
