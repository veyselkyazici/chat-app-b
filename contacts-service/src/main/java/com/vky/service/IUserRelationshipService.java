package com.vky.service;

import com.vky.dto.request.ContactInformationOfExistingChatsRequestDTO;
import com.vky.dto.response.UserProfileResponseDTO;
import com.vky.repository.entity.Contacts;
import com.vky.repository.entity.UserRelationship;

import java.util.List;
import java.util.UUID;

public interface IUserRelationshipService {
    void updateUserRelationship(UUID userId, UUID userContactId, Contacts contact);

    UserRelationship handleUserRelationship(UserProfileResponseDTO userProfileResponseDTO, UUID uuiDuserId);

    UserRelationship handleReverseUserRelationship(UserProfileResponseDTO userProfileResponseDTO, UUID uuiDuserId);

    List<UserRelationship> getContactInformationOfExistingChats(
            ContactInformationOfExistingChatsRequestDTO contactInformationOfExistingChatsRequestDTO);

    UserRelationship saveUserRelationship(UUID inviterUserId, UserProfileResponseDTO userProfile,
            boolean userHasAddedRelatedUser);

    List<UserRelationship> findByUserIdOrRelatedUserId(UUID userId);

    void publishRelationshipSyncForUser(UUID userId);

    List<UserRelationship> findByUserId(UUID userId);
}
