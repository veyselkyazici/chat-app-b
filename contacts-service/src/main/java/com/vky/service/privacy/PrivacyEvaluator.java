package com.vky.service.privacy;

import com.vky.dto.response.PrivacySettingsResponseDTO;
import com.vky.repository.entity.enums.VisibilityOption;
import org.springframework.stereotype.Component;

@Component
public class PrivacyEvaluator {

    public boolean canSee(
            PrivacySettingsResponseDTO settings,
            RelationshipContext ctx,
            PrivacyField field) {
        VisibilityOption visibility = switch (field) {
            case PROFILE_PHOTO -> settings.profilePhotoVisibility();
            case LAST_SEEN -> settings.lastSeenVisibility();
            case ONLINE_STATUS -> settings.onlineStatusVisibility();
            case ABOUT -> settings.aboutVisibility();
        };

        return switch (visibility) {
            case EVERYONE -> true;
            case MY_CONTACTS -> ctx.isMutuallyConnected();
            case NOBODY -> false;
        };
    }
}
