package com.vky.service.privacy;

import com.vky.dto.response.PrivacySettingsResponseDTO;
import com.vky.dto.response.UserProfileResponseDTO;
import com.vky.manager.IUserManager;
import com.vky.repository.IUserRelationshipRepository;
import com.vky.repository.entity.UserRelationship;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrivacyFilteringService {

    private final IUserRelationshipRepository relationshipRepository;
    private final RelationshipContextBuilder contextBuilder;
    private final PrivacyEvaluator evaluator;
    private final IUserManager userManager;

    public List<UUID> resolveAllowedProfilePhotoViewers(UUID ownerId) {

        UserProfileResponseDTO owner = userManager.getUserById(ownerId);
        PrivacySettingsResponseDTO settings = owner.getPrivacySettings();

        List<UserRelationship> relations =
                relationshipRepository.findByUserIdOrRelatedUserId(ownerId);

        List<UUID> candidates = relations.stream()
                .map(rel -> rel.getUserId().equals(ownerId)
                        ? rel.getRelatedUserId()
                        : rel.getUserId())
                .toList();

        List<UUID> allowed = new ArrayList<>();

        for (UUID targetId : candidates) {
            RelationshipContext ctx = contextBuilder.build(ownerId, targetId);

            if (evaluator.canSeeProfilePhoto(settings, ctx)) {
                allowed.add(targetId);
            }
        }

        return allowed;
    }
}

