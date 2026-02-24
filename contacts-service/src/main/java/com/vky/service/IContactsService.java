package com.vky.service;

import com.vky.dto.RelationshipSnapshotDTO;
import com.vky.dto.request.ContactInformationOfExistingChatRequestDTO;
import com.vky.dto.request.ContactInformationOfExistingChatsRequestDTO;
import com.vky.dto.request.ContactRequestDTO;
import com.vky.dto.response.ContactResponseDTO;
import com.vky.dto.response.UserProfileResponseDTO;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IContactsService {
    void deleteContact(UUID id, String userId);

    void addContact(ContactRequestDTO dto, String userId);

    CompletableFuture<List<ContactResponseDTO>> getContactList(String tokenUserId);

    CompletableFuture<List<ContactResponseDTO>> getContactInformationOfExistingChats(
            ContactInformationOfExistingChatsRequestDTO contactInformationOfExistingChatsRequestDTO);

    CompletableFuture<ContactResponseDTO> getContactInformationOfSingleChat(
            ContactInformationOfExistingChatRequestDTO contactInformationOfExistingChatRequestDTO);

    void checkUsersWhoInvited(UserProfileResponseDTO userProfile);

    RelationshipSnapshotDTO snapshot(UUID userId);
}
