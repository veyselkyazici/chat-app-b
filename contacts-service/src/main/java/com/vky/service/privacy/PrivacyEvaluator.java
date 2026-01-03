package com.vky.service.privacy;

import com.vky.dto.response.PrivacySettingsResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class PrivacyEvaluator {

    public boolean canSeeProfilePhoto(PrivacySettingsResponseDTO settings, RelationshipContext ctx) {

        return switch (settings.getProfilePhotoVisibility()) {
            case EVERYONE -> true;
            case MY_CONTACTS -> ctx.isMutuallyConnected();
            case NOBODY -> false;
        };
    }

    public boolean canSeeLastSeen(PrivacySettingsResponseDTO settings, RelationshipContext ctx) {

        return switch (settings.getLastSeenVisibility()) {
            case EVERYONE -> true;
            case MY_CONTACTS -> ctx.isMutuallyConnected();
            case NOBODY -> false;
        };
    }

    public boolean canSeeOnlineStatus(PrivacySettingsResponseDTO settings, RelationshipContext ctx) {

        return switch (settings.getOnlineStatusVisibility()) {
            case EVERYONE -> true;
            case MY_CONTACTS -> ctx.isMutuallyConnected();
            case NOBODY -> false;
        };
    }

    public boolean canSeeAbout(PrivacySettingsResponseDTO settings, RelationshipContext ctx) {

        return switch (settings.getAboutVisibility()) {
            case EVERYONE -> true;
            case MY_CONTACTS -> ctx.isMutuallyConnected();
            case NOBODY -> false;
        };
    }
}




