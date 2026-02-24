package com.vky.service;

import com.vky.dto.RelationshipSyncEvent;
import com.vky.dto.request.ContactInformationOfExistingChatsRequestDTO;
import com.vky.dto.response.UserProfileResponseDTO;
import com.vky.exception.ContactsServiceException;
import com.vky.exception.ErrorType;
import com.vky.rabbitmq.RabbitMQProducer;
import com.vky.repository.IUserRelationshipRepository;
import com.vky.repository.entity.Contacts;
import com.vky.repository.entity.UserRelationship;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserRelationshipServiceImpl implements IUserRelationshipService {
    private final IUserRelationshipRepository userRelationshipRepository;
    private final RabbitMQProducer rabbitMQProducer;

    @Override
    public void updateUserRelationship(UUID userId, UUID userContactId, Contacts contact) {
        UserRelationship userRelationship = userRelationshipRepository
                .findRelationshipBetweenUsers(userId, userContactId)
                .orElseThrow(
                        () -> new ContactsServiceException(ErrorType.CONTACT_NOT_FOUND, contact.getId().toString()));
        if (userRelationship.getUserId().equals(contact.getUserId())) {
            userRelationship.setUserHasAddedRelatedUser(false);
        } else {
            userRelationship.setRelatedUserHasAddedUser(false);
        }
        userRelationshipRepository.save(userRelationship);
    }

    @Override
    public UserRelationship handleUserRelationship(UserProfileResponseDTO userProfileResponseDTO, UUID uuiDuserId) {
        Optional<UserRelationship> relationshipOpt = userRelationshipRepository.findByUserIdAndRelatedUserId(
                uuiDuserId,
                userProfileResponseDTO.id());

        if (relationshipOpt.isPresent()) {
            UserRelationship relationship = relationshipOpt.get();
            relationship.setUserHasAddedRelatedUser(true);
            return userRelationshipRepository.save(relationship);
        } else {
            UserRelationship newRelationship = new UserRelationship();
            newRelationship.setUserId(uuiDuserId);
            newRelationship.setRelatedUserId(userProfileResponseDTO.id());
            newRelationship.setUserHasAddedRelatedUser(true);
            newRelationship.setRelatedUserHasAddedUser(false);
            return userRelationshipRepository.save(newRelationship);
        }
    }

    @Override
    public UserRelationship handleReverseUserRelationship(UserProfileResponseDTO userProfileResponseDTO,
            UUID uuiDuserId) {
        Optional<UserRelationship> reverseRelationshipOpt = userRelationshipRepository.findByUserIdAndRelatedUserId(
                userProfileResponseDTO.id(),
                uuiDuserId);

        if (reverseRelationshipOpt.isPresent()) {
            UserRelationship reverseRelationship = reverseRelationshipOpt.get();
            reverseRelationship.setRelatedUserHasAddedUser(true);
            return userRelationshipRepository.save(reverseRelationship);
        }
        return null;
    }

    @Override
    public List<UserRelationship> getContactInformationOfExistingChats(
            ContactInformationOfExistingChatsRequestDTO contactInformationOfExistingChatsRequestDTO) {
        return userRelationshipRepository.findRelationshipsForUser(
                contactInformationOfExistingChatsRequestDTO.userId(),
                contactInformationOfExistingChatsRequestDTO.userContactIds());
    }

    @Override
    public UserRelationship saveUserRelationship(UUID inviterUserId, UserProfileResponseDTO userProfile,
            boolean userHasAddedRelatedUser) {
        UserRelationship userRelationship = new UserRelationship();
        userRelationship.setUserId(inviterUserId);
        userRelationship.setRelatedUserId(userProfile.id());
        userRelationship.setUserHasAddedRelatedUser(userHasAddedRelatedUser);
        userRelationship.setRelatedUserHasAddedUser(false);
        userRelationshipRepository.save(userRelationship);
        return userRelationship;
    }

    @Override
    public List<UserRelationship> findByUserIdOrRelatedUserId(UUID userId) {
        return userRelationshipRepository.findByUserIdOrRelatedUserId(userId);
    }

    @Override
    public void publishRelationshipSyncForUser(UUID userId) {
        List<UserRelationship> relations = userRelationshipRepository.findByUserIdOrRelatedUserId(userId);

        List<String> relatedUserIds = relations.stream()
                .map(rel -> rel.getUserId().equals(userId)
                        ? rel.getRelatedUserId().toString()
                        : rel.getUserId().toString())
                .distinct()
                .toList();

        List<String> outgoingContactIds = relations.stream()
                .filter(rel -> {
                    if (rel.getUserId().equals(userId)) {
                        return rel.isUserHasAddedRelatedUser();
                    } else {
                        return rel.isRelatedUserHasAddedUser();
                    }
                })
                .map(rel -> rel.getUserId().equals(userId)
                        ? rel.getRelatedUserId().toString()
                        : rel.getUserId().toString())
                .distinct()
                .toList();

        RelationshipSyncEvent event = RelationshipSyncEvent.builder()
                .userId(userId.toString())
                .relatedUserIds(relatedUserIds)
                .outgoingContactIds(outgoingContactIds)
                .build();

        rabbitMQProducer.publishRelationshipSync(event);
    }

    @Override
    public List<UserRelationship> findByUserId(UUID userId) {
        return userRelationshipRepository.findByUserId(userId);
    }
}
