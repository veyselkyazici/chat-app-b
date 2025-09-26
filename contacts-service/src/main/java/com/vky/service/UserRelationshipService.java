package com.vky.service;

import com.vky.dto.request.ContactInformationOfExistingChatsRequestDTO;
import com.vky.dto.request.UpdatePrivacySettingsRequestDTO;
import com.vky.dto.request.UpdatedProfilePhotoRequestDTO;
import com.vky.dto.response.UserProfileResponseDTO;
import com.vky.exception.ContactsServiceException;
import com.vky.exception.ErrorType;
import com.vky.repository.IUserRelationshipRepository;
import com.vky.repository.entity.Contacts;
import com.vky.repository.entity.UserRelationship;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserRelationshipService {
    private final IUserRelationshipRepository userRelationshipRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void updateUserRelationship(UUID userId, UUID userContactId, Contacts contact) {
        UserRelationship userRelationship = userRelationshipRepository.findRelationshipBetweenUsers(userId, userContactId)
                .orElseThrow(() -> new ContactsServiceException(ErrorType.CONTACT_NOT_FOUND,contact.getId().toString()));
        if(userRelationship.getUserId().equals(contact.getUserId())) {
            userRelationship.setUserHasAddedRelatedUser(false);
        } else {
            userRelationship.setRelatedUserHasAddedUser(false);
        }
        userRelationshipRepository.save(userRelationship);
    }

    public void sendUpdatedPrivacySettings(UpdatePrivacySettingsRequestDTO updatePrivacySettingsRequestDTO) {
        List<UserRelationship> userRelationships = userRelationshipRepository.findByUserIdOrRelatedUserId(updatePrivacySettingsRequestDTO.getId());

        userRelationships.forEach(userRelationship -> {
            if (userRelationship.getUserId().equals(updatePrivacySettingsRequestDTO.getId())) {
                messagingTemplate.convertAndSendToUser(userRelationship.getRelatedUserId().toString(), "/queue/updated-privacy-response", updatePrivacySettingsRequestDTO);
            } else {
                messagingTemplate.convertAndSendToUser(userRelationship.getUserId().toString(), "/queue/updated-privacy-response", updatePrivacySettingsRequestDTO);

            }
        });
    }

    public void sendUpdatedProfilePhoto(UpdatedProfilePhotoRequestDTO dto) {
        List<UserRelationship> userRelationships = userRelationshipRepository.findByUserIdOrRelatedUserId(dto.getUserId());
        userRelationships.forEach(userRelationship -> {
            if (userRelationship.getUserId().equals(dto.getUserId())) {
                messagingTemplate.convertAndSendToUser(userRelationship.getRelatedUserId().toString(), "/queue/updated-profile-photo-message", dto);
            } else {
                messagingTemplate.convertAndSendToUser(userRelationship.getUserId().toString(), "/queue/updated-profile-photo-message", dto);
            }
        });
    }

    public UserRelationship handleUserRelationship(UserProfileResponseDTO userProfileResponseDTO, UUID uuiDuserId) {
        Optional<UserRelationship> relationshipOpt = userRelationshipRepository.findByUserIdAndRelatedUserId(
                uuiDuserId,
                userProfileResponseDTO.getId()
        );

        if (relationshipOpt.isPresent()) {
            UserRelationship relationship = relationshipOpt.get();
            relationship.setUserHasAddedRelatedUser(true);
            return userRelationshipRepository.save(relationship);
        } else {
            UserRelationship newRelationship = new UserRelationship();
            newRelationship.setUserId(uuiDuserId);
            newRelationship.setRelatedUserId(userProfileResponseDTO.getId());
            newRelationship.setUserHasAddedRelatedUser(true);
            newRelationship.setRelatedUserHasAddedUser(false);
            return userRelationshipRepository.save(newRelationship);
        }
    }

    public UserRelationship handleReverseUserRelationship(UserProfileResponseDTO userProfileResponseDTO, UUID uuiDuserId) {
        Optional<UserRelationship> reverseRelationshipOpt = userRelationshipRepository.findByUserIdAndRelatedUserId(
                userProfileResponseDTO.getId(),
                uuiDuserId
        );

        if (reverseRelationshipOpt.isPresent()) {
            UserRelationship reverseRelationship = reverseRelationshipOpt.get();
            reverseRelationship.setRelatedUserHasAddedUser(true);
            return userRelationshipRepository.save(reverseRelationship);
        }
        return null;
    }

    public List<UserRelationship> getContactInformationOfExistingChats(ContactInformationOfExistingChatsRequestDTO contactInformationOfExistingChatsRequestDTO) {
        return userRelationshipRepository.findRelationshipsForUser(
                contactInformationOfExistingChatsRequestDTO.getUserId(), contactInformationOfExistingChatsRequestDTO.getUserContactIds());
    }

    public UserRelationship saveUserRelationship(UUID inviterUserId, UserProfileResponseDTO userProfile, boolean userHasAddedRelatedUser) {
        UserRelationship userRelationship = new UserRelationship();
        userRelationship.setUserId(inviterUserId);
        userRelationship.setRelatedUserId(userProfile.getId());
        userRelationship.setUserHasAddedRelatedUser(userHasAddedRelatedUser);
        userRelationship.setRelatedUserHasAddedUser(false);
        userRelationshipRepository.save(userRelationship);
        return userRelationship;
    }
}
