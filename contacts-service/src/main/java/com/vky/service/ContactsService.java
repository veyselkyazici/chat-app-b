package com.vky.service;

import com.vky.dto.RelationshipSnapshotDTO;
import com.vky.dto.request.ContactInformationOfExistingChatRequestDTO;
import com.vky.dto.request.ContactInformationOfExistingChatsRequestDTO;
import com.vky.dto.request.ContactRequestDTO;
import com.vky.dto.response.*;
import com.vky.exception.ContactsServiceException;
import com.vky.exception.ErrorType;
import com.vky.manager.IUserManager;
import com.vky.mapper.IInvitationMapper;
import com.vky.rabbitmq.RabbitMQProducer;
import com.vky.repository.ContactWithRelationshipDTO;
import com.vky.repository.IContactsRepository;
import com.vky.repository.entity.Contacts;
import com.vky.repository.entity.Invitation;
import com.vky.repository.entity.UserRelationship;
import com.vky.repository.entity.enums.VisibilityOption;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactsService {
        private final IContactsRepository contactsRepository;
        private final IUserManager userManager;
        private final InvitationService invitationService;
        private final UserRelationshipService userRelationshipService;
        private final RabbitMQProducer rabbitMQProducer;

        @Transactional
        public void deleteContact(UUID id, String userId) {
                Contacts contact = contactsRepository.findById(id)
                                .orElseThrow(() -> new ContactsServiceException(ErrorType.CONTACT_NOT_FOUND));

                contact.setDeleted(true);
                contact.setUserContactName(null);
                contactsRepository.save(contact);
                userRelationshipService.updateUserRelationship(UUID.fromString(userId), contact.getUserContactId(),
                                contact);
                userRelationshipService.publishRelationshipSyncForUser(UUID.fromString(userId));
                rabbitMQProducer.publishContactDeleted(contact.getUserContactId().toString(), userId);
        }

        public void addContact(ContactRequestDTO dto, String userId) {

                UserProfileResponseDTO profile = userManager.getUserByEmail(dto.userContactEmail(), userId);
                if (profile == null) {
                        handleInvitationProcess(dto, userId);
                        return;
                }

                if (userId.equals(profile.id().toString())) {
                        throw new ContactsServiceException(ErrorType.CANNOT_ADD_SELF_AS_CONTACT);
                }

                handleExistingContactProcess(dto, profile, userId);

                userRelationshipService.publishRelationshipSyncForUser(UUID.fromString(userId));
                userRelationshipService.publishRelationshipSyncForUser(profile.id());
        }

        private void handleInvitationProcess(ContactRequestDTO contactRequestDTO, String userId) {
                if (invitationService.isExistsInvitation(UUID.fromString(userId),
                                contactRequestDTO.userContactEmail())) {
                        throw new ContactsServiceException(ErrorType.INVITATION_ALREADY);
                } else {
                        Invitation invitation = invitationService.addInvitation(contactRequestDTO, userId);
                        ContactResponseDTO dto = ContactResponseDTO.builder()
                                        .invitationResponseDTO(new InvitationResponseDTO(invitation.getId(),
                                                        invitation.isInvited(),
                                                        invitation.getContactName(), invitation.getInviterUserId(),
                                                        invitation.getInviteeEmail()))
                                        .build();
                        rabbitMQProducer.publishAddInvitation(dto, invitation.getInviterUserId().toString());
                }
        }

        private void handleExistingContactProcess(ContactRequestDTO contactRequestDTO,
                        UserProfileResponseDTO userProfileResponseDTO, String userId) {
                UUID UUIDuserId = UUID.fromString(userId);
                Optional<Contacts> existingContactOpt = contactsRepository
                                .findContactsByUserContactEmailAndUserIdAndIsDeletedFalse(
                                                userProfileResponseDTO.email(),
                                                UUIDuserId);

                Contacts contact = existingContactOpt.map(existingContact -> {
                        if (!existingContact.isDeleted()) {
                                throw new ContactsServiceException(
                                                ErrorType.CONTACT_ALREADY);
                        }
                        existingContact.setDeleted(false);
                        existingContact.setUserContactName(contactRequestDTO.userContactName());
                        return contactsRepository.save(existingContact);
                }).orElseGet(() -> {
                        if (UUIDuserId.equals(userProfileResponseDTO.id())) {
                                throw new IllegalArgumentException("User contact ID cannot be null");
                        }
                        return contactsRepository.save(
                                        Contacts.builder()
                                                        .userContactEmail(contactRequestDTO.userContactEmail())
                                                        .userContactName(contactRequestDTO.userContactName())
                                                        .userId(UUIDuserId)
                                                        .userContactId(userProfileResponseDTO.id())
                                                        .build());
                });
                UserRelationship reverseRelationship = handleReverseUserRelationship(userProfileResponseDTO,
                                UUIDuserId);
                UserRelationship relationship = (reverseRelationship == null)
                                ? handleUserRelationship(userProfileResponseDTO, UUIDuserId)
                                : null;
                ContactResponseDTO contactResponseDTO = createContactResponseDTO(contact, relationship,
                                reverseRelationship,
                                userProfileResponseDTO);
                rabbitMQProducer.publishContactAdded(contactResponseDTO, contact.getUserId().toString());

                UserProfileResponseDTO updatedProfile = userProfileResponseDTO.toBuilder()
                                .image(contactRequestDTO.image())
                                .build();
                ContactResponseDTO updatedContactResponseDTO = contactResponseDTO.toBuilder()
                                .userProfileResponseDTO(updatedProfile).build();

                rabbitMQProducer.publishContactAddedUser(updatedContactResponseDTO,
                                contact.getUserContactId().toString());
        }

        private UserRelationship handleUserRelationship(UserProfileResponseDTO userProfileResponseDTO,
                        UUID UUIDuserId) {
                return userRelationshipService.handleUserRelationship(userProfileResponseDTO, UUIDuserId);

        }

        private UserRelationship handleReverseUserRelationship(UserProfileResponseDTO userProfileResponseDTO,
                        UUID UUIDuserId) {
                return userRelationshipService.handleReverseUserRelationship(userProfileResponseDTO, UUIDuserId);
        }

        private ContactResponseDTO createContactResponseDTO(Contacts contact, UserRelationship relationship,
                        UserRelationship reverseRelationship, UserProfileResponseDTO userProfileResponseDTO) {

                UserKeyResponseDTO newUserKey = UserKeyResponseDTO.builder()
                                .iv(userProfileResponseDTO.userKey().iv())
                                .salt(userProfileResponseDTO.userKey().salt())
                                .publicKey(userProfileResponseDTO.userKey().publicKey())
                                .encryptedPrivateKey(userProfileResponseDTO.userKey().encryptedPrivateKey())
                                .build();

                UserProfileResponseDTO updatedProfile = userProfileResponseDTO.toBuilder()
                                .userKey(newUserKey)
                                .build();

                return ContactResponseDTO.builder()
                                .contactsDTO(ContactsDTO.builder()
                                                .id(contact.getId())
                                                .userId(contact.getUserId())
                                                .userContactId(contact.getUserContactId())
                                                .userContactName(contact.getUserContactName())
                                                .relatedUserHasAddedUser(relationship != null
                                                                ? relationship.isRelatedUserHasAddedUser()
                                                                : reverseRelationship.isUserHasAddedRelatedUser())
                                                .userHasAddedRelatedUser(relationship != null
                                                                ? relationship.isUserHasAddedRelatedUser()
                                                                : reverseRelationship.isUserHasAddedRelatedUser())
                                                .build())
                                .userProfileResponseDTO(updatedProfile)
                                .build();
        }

        @Transactional(readOnly = true)
        @Async("taskExecutor")
        public CompletableFuture<List<ContactResponseDTO>> getContactList(String tokenUserId) {
                UUID userId = UUID.fromString(tokenUserId);
                List<Invitation> invitations = this.invitationService
                                .findInvitationByInviterUserIdOrderByContactName(userId);
                List<ContactWithRelationshipDTO> dto1 = contactsRepository.findContactsAndRelationshipsByUserId(userId);

                Map<UUID, ContactWithRelationshipDTO> contactMap = dto1.stream()
                                .collect(Collectors.toMap(
                                                ContactWithRelationshipDTO::getUserContactId,
                                                Function.identity(),
                                                (existing, replacement) -> existing,
                                                LinkedHashMap::new));

                List<UUID> orderedIds = new ArrayList<>(contactMap.keySet());

                List<ContactResponseDTO> userResponseDTOS = userManager.getUsers(orderedIds, tokenUserId);

                Map<UUID, ContactResponseDTO> responseMap = userResponseDTOS.stream()
                                .collect(Collectors.toMap(
                                                response -> response.userProfileResponseDTO().id(),
                                                Function.identity()));

                List<ContactResponseDTO> orderedResults = new ArrayList<>();

                for (UUID orderedId : orderedIds) {
                        ContactResponseDTO userResponse = responseMap.get(orderedId);
                        if (userResponse != null) {
                                ContactWithRelationshipDTO correspondingContact = contactMap.get(orderedId);

                                // Privacy is now handled server-side in user-service

                                if (correspondingContact != null) {
                                        ContactsDTO contactsDTO = ContactsDTO.builder()
                                                        .id(correspondingContact.getId())
                                                        .userId(correspondingContact.getUserId())
                                                        .userContactId(correspondingContact.getUserContactId())
                                                        .userContactName(correspondingContact.getUserContactName())
                                                        .userHasAddedRelatedUser(correspondingContact
                                                                        .getUserHasAddedRelatedUser())
                                                        .relatedUserHasAddedUser(correspondingContact
                                                                        .getRelatedUserHasAddedUser())
                                                        .build();
                                        userResponse = userResponse.toBuilder().contactsDTO(contactsDTO).build();
                                }

                                orderedResults.add(userResponse);
                        }
                }

                List<ContactResponseDTO> invitationResponseDTOS = invitations.stream()
                                .map(this::convertInvitationToContact)
                                .toList();

                orderedResults.addAll(invitationResponseDTOS);

                return CompletableFuture.completedFuture(orderedResults);
        }

        @Async("taskExecutor")
        public CompletableFuture<List<ContactResponseDTO>> getContactInformationOfExistingChats(
                        ContactInformationOfExistingChatsRequestDTO contactInformationOfExistingChatsRequestDTO) {

                List<UserRelationship> relationships = userRelationshipService
                                .getContactInformationOfExistingChats(contactInformationOfExistingChatsRequestDTO);

                List<Contacts> contacts = contactsRepository.findContactsForUser(
                                contactInformationOfExistingChatsRequestDTO.userId(),
                                contactInformationOfExistingChatsRequestDTO.userContactIds());

                Map<UUID, Contacts> contactMap = contacts.stream()
                                .collect(Collectors.toMap(
                                                Contacts::getUserContactId,
                                                Function.identity(),
                                                (existing, replacement) -> existing));

                Map<UUID, ContactWithRelationshipDTO> contactDTOMap = new HashMap<>();
                for (UserRelationship ur : relationships) {
                        UUID relatedUserId = ur.getUserId().equals(contactInformationOfExistingChatsRequestDTO.userId())
                                        ? ur.getRelatedUserId()
                                        : ur.getUserId();

                        Contacts contact = contactMap.get(relatedUserId);

                        String userContactName = contact != null ? contact.getUserContactName() : null;

                        boolean userHasAdded = ur.getUserId()
                                        .equals(contactInformationOfExistingChatsRequestDTO.userId())
                                                        ? ur.isUserHasAddedRelatedUser()
                                                        : ur.isRelatedUserHasAddedUser();

                        boolean relatedHasAdded = ur.getUserId()
                                        .equals(contactInformationOfExistingChatsRequestDTO.userId())
                                                        ? ur.isRelatedUserHasAddedUser()
                                                        : ur.isUserHasAddedRelatedUser();

                        ContactWithRelationshipDTO dto = new ContactWithRelationshipDTO();
                        dto.setUserId(contactInformationOfExistingChatsRequestDTO.userId());
                        dto.setUserContactId(relatedUserId);
                        dto.setUserContactName(userContactName);
                        dto.setUserHasAddedRelatedUser(userHasAdded);
                        dto.setRelatedUserHasAddedUser(relatedHasAdded);
                        dto.setId(contact != null ? contact.getId() : null);

                        contactDTOMap.put(relatedUserId, dto);
                }

                List<ContactResponseDTO> userResponseDTOS = userManager
                                .getUsers(new ArrayList<>(contactDTOMap.keySet()),
                                                contactInformationOfExistingChatsRequestDTO.userId().toString());

                List<ContactResponseDTO> filteredDtos = userResponseDTOS.stream()
                                .map(user -> {
                                        ContactWithRelationshipDTO contact = contactDTOMap
                                                        .get(user.userProfileResponseDTO().id());
                                        return ContactResponseDTO.builder()
                                                        .userProfileResponseDTO(user.userProfileResponseDTO())
                                                        .contactsDTO(ContactsDTO.builder()
                                                                        .userContactName(contact.getUserContactName())
                                                                        .id(contact.getId())
                                                                        .userHasAddedRelatedUser(contact
                                                                                        .getUserHasAddedRelatedUser())
                                                                        .relatedUserHasAddedUser(contact
                                                                                        .getRelatedUserHasAddedUser())
                                                                        .userContactId(contact.getUserContactId())
                                                                        .userId(contact.getUserId())
                                                                        .build())
                                                        .build();
                                })
                                .collect(Collectors.toList());

                return CompletableFuture.completedFuture(filteredDtos);

        }

        @Async("taskExecutor")
        public CompletableFuture<ContactResponseDTO> getContactInformationOfSingleChat(
                        ContactInformationOfExistingChatRequestDTO contactInformationOfExistingChatRequestDTO) {
                Optional<ContactWithRelationshipDTO> optionalContact = contactsRepository.findContactWithRelationship(
                                contactInformationOfExistingChatRequestDTO.userId(),
                                contactInformationOfExistingChatRequestDTO.userContactId());
                ContactWithRelationshipDTO contact;
                UserProfileResponseDTO userProfileResponseDTO = this.userManager
                                .getUserById(contactInformationOfExistingChatRequestDTO.userContactId(),
                                                contactInformationOfExistingChatRequestDTO.userId().toString());

                if (optionalContact.isPresent()) {
                        contact = optionalContact.get();
                        if (contact.getUserId().equals(contactInformationOfExistingChatRequestDTO.userId())) {
                                return CompletableFuture.completedFuture(ContactResponseDTO.builder()
                                                .userProfileResponseDTO(userProfileResponseDTO)
                                                .contactsDTO(ContactsDTO.builder()
                                                                .userContactName(contact.getUserContactName())
                                                                .id(contact.getId())
                                                                .userHasAddedRelatedUser(
                                                                                contact.getUserHasAddedRelatedUser())
                                                                .relatedUserHasAddedUser(
                                                                                contact.getRelatedUserHasAddedUser())
                                                                .userContactId(contact.getUserContactId())
                                                                .userId(contact.getUserId())
                                                                .build())
                                                .build());
                        } else {

                                return CompletableFuture.completedFuture(ContactResponseDTO.builder()
                                                .userProfileResponseDTO(userProfileResponseDTO)
                                                .contactsDTO(ContactsDTO.builder()
                                                                .userContactName(null)
                                                                .id(contact.getId())
                                                                .userHasAddedRelatedUser(
                                                                                contact.getRelatedUserHasAddedUser())
                                                                .relatedUserHasAddedUser(
                                                                                contact.getUserHasAddedRelatedUser())
                                                                .userContactId(contact.getUserId())
                                                                .userId(contact.getUserContactId())
                                                                .build())
                                                .build());
                        }
                } else {
                        return CompletableFuture.completedFuture(ContactResponseDTO.builder()
                                        .userProfileResponseDTO(userProfileResponseDTO)
                                        .contactsDTO(ContactsDTO.builder()
                                                        .userContactName(null)
                                                        .id(null)
                                                        .userHasAddedRelatedUser(false)
                                                        .relatedUserHasAddedUser(false)
                                                        .userContactId(contactInformationOfExistingChatRequestDTO
                                                                        .userContactId())
                                                        .userId(contactInformationOfExistingChatRequestDTO.userId())
                                                        .build())
                                        .build());
                }
        }

        private ContactResponseDTO convertInvitationToContact(Invitation invitation) {
                InvitationResponseDTO invitationResponseDTO = IInvitationMapper.INSTANCE
                                .toInvitationResponseDTO(invitation);
                return ContactResponseDTO.builder()
                                .invitationResponseDTO(invitationResponseDTO)
                                .build();
        }

        @Transactional
        public void checkUsersWhoInvited(UserProfileResponseDTO userProfile) {
                List<Invitation> invitations = invitationService.getInvitations(userProfile.email());

                invitations.forEach(invitation -> {
                        UserRelationship userRelationship = userRelationshipService
                                        .saveUserRelationship(invitation.getInviterUserId(), userProfile, true);

                        Contacts contact = saveContacts(invitation, userProfile.id());
                        ContactsDTO contactsDTO = getContactsDTO(contact, userRelationship);

                        ContactResponseDTO contactResponseDTO = ContactResponseDTO.builder()
                                        .contactsDTO(contactsDTO)
                                        .userProfileResponseDTO(userProfile)
                                        .invitationResponseDTO(null)
                                        .build();

                        invitation.setDeleted(true);
                        invitationService.saveInvitation(invitation);
                        rabbitMQProducer.publishInvitedUserJoined(contactResponseDTO, contact.getUserId().toString());
                });
        }

        private ContactsDTO getContactsDTO(Contacts contact, UserRelationship userRelationship) {
                return ContactsDTO.builder()
                                .id(contact.getId())
                                .userId(contact.getUserId())
                                .userContactId(contact.getUserContactId())
                                .userContactName(contact.getUserContactName())
                                .userHasAddedRelatedUser(userRelationship.isUserHasAddedRelatedUser())
                                .relatedUserHasAddedUser(userRelationship.isRelatedUserHasAddedUser())
                                .build();
        }

        private Contacts saveContacts(Invitation invitation, UUID invitedUserId) {
                Contacts contacts = new Contacts();
                contacts.setUserId(invitation.getInviterUserId());
                contacts.setUserContactEmail(invitation.getInviteeEmail());
                contacts.setUserContactId(invitedUserId);
                contacts.setUserContactName(invitation.getContactName());
                contacts.setUserEmail(invitation.getInviterEmail());
                return contactsRepository.save(contacts);
        }

        public RelationshipSnapshotDTO snapshot(UUID userId) {
                List<UserRelationship> relations = userRelationshipService.findByUserIdOrRelatedUserId(userId);
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

                return new RelationshipSnapshotDTO(userId.toString(), relatedUserIds, outgoingContactIds);
        }
}
