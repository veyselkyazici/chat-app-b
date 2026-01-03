package com.vky.service.privacy;

import com.vky.repository.IUserRelationshipRepository;
import com.vky.repository.entity.UserRelationship;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RelationshipContextBuilder {

    private final IUserRelationshipRepository relationshipRepository;

    public RelationshipContext build(UUID ownerId, UUID targetId) {

        Optional<UserRelationship> relOpt =
                relationshipRepository.findRelationshipBetweenUsers(ownerId, targetId);

        boolean ownerAdded = false;
        boolean targetAdded = false;

        if (relOpt.isPresent()) {
            UserRelationship rel = relOpt.get();

            if (rel.getUserId().equals(ownerId)) {
                ownerAdded = rel.isUserHasAddedRelatedUser();
                targetAdded = rel.isRelatedUserHasAddedUser();
            } else {
                ownerAdded = rel.isRelatedUserHasAddedUser();
                targetAdded = rel.isUserHasAddedRelatedUser();
            }
        }

        return RelationshipContext.builder()
                .ownerUserId(ownerId)
                .targetUserId(targetId)
                .ownerAddedTarget(ownerAdded)
                .targetAddedOwner(targetAdded)
                .mutuallyConnected(ownerAdded && targetAdded)
                .build();
    }
}



