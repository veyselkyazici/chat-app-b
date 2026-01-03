package com.vky.service;

import com.vky.dto.request.*;
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
        userRelationshipService.updateUserRelationship(UUID.fromString(userId), contact.getUserContactId(),contact);
    }

    public void addContact(ContactRequestDTO dto, String userId) {

        UserProfileResponseDTO profile = userManager.getUserByEmail(dto.getUserContactEmail());
        if (profile == null) {
            handleInvitationProcess(dto, userId);
            return;
        }

        if (userId.equals(profile.getId().toString())) {
            throw new ContactsServiceException(ErrorType.CANNOT_ADD_SELF_AS_CONTACT);
        }


        handleExistingContactProcess(dto, profile, userId);

        userRelationshipService.publishRelationshipSyncForUser(UUID.fromString(userId));
        userRelationshipService.publishRelationshipSyncForUser(profile.getId());
    }



    private void handleInvitationProcess(ContactRequestDTO contactRequestDTO, String userId) {
        if (invitationService.isExistsInvitation(UUID.fromString(userId), contactRequestDTO.getUserContactEmail())) {
            throw new ContactsServiceException(ErrorType.INVITATION_ALREADY);
        } else {
            Invitation invitation = invitationService.addInvitation(contactRequestDTO, userId);
            ContactResponseDTO dto = new ContactResponseDTO();
            dto.setInvitationResponseDTO(new InvitationResponseDTO(invitation.getId(),invitation.isInvited(),invitation.getContactName(),invitation.getInviterUserId(), invitation.getInviteeEmail()));
            rabbitMQProducer.publishAddInvitation(dto, invitation.getInviterUserId().toString());
        }
    }

    private void handleExistingContactProcess(ContactRequestDTO contactRequestDTO, UserProfileResponseDTO userProfileResponseDTO, String userId) {
        UUID UUIDuserId = UUID.fromString(userId);
        Optional<Contacts> existingContactOpt = contactsRepository.findContactsByUserContactEmailAndUserIdAndIsDeletedFalse(
                userProfileResponseDTO.getEmail(),
                UUIDuserId
        );

        Contacts contact = existingContactOpt.map(existingContact -> {
            if (!existingContact.isDeleted()) {
                throw new ContactsServiceException(
                        ErrorType.CONTACT_ALREADY
                );
            }
            existingContact.setDeleted(false);
            existingContact.setUserContactName(contactRequestDTO.getUserContactName());
            return contactsRepository.save(existingContact);
        }).orElseGet(() -> {
            if (UUIDuserId.equals(userProfileResponseDTO.getId())) {
                throw new IllegalArgumentException("User contact ID cannot be null");
            }
            return contactsRepository.save(
                    Contacts.builder()
                            .userContactEmail(contactRequestDTO.getUserContactEmail())
                            .userContactName(contactRequestDTO.getUserContactName())
                            .userId(UUIDuserId)
                            .userContactId(userProfileResponseDTO.getId())
                            .build()
            );
        });
        UserRelationship reverseRelationship = handleReverseUserRelationship(userProfileResponseDTO, UUIDuserId);
        UserRelationship relationship = (reverseRelationship == null) ? handleUserRelationship(userProfileResponseDTO, UUIDuserId) : null;
        ContactResponseDTO contactResponseDTO = createContactResponseDTO(contact, relationship, reverseRelationship, userProfileResponseDTO);
        rabbitMQProducer.publishContactAdded(contactResponseDTO,contact.getUserId().toString());
        userProfileResponseDTO.setImagee(contactRequestDTO.getImagee());
        rabbitMQProducer.publishContactAddedUser(contactResponseDTO,contact.getUserContactId().toString());
    }

    private UserRelationship handleUserRelationship(UserProfileResponseDTO userProfileResponseDTO, UUID UUIDuserId) {
        return userRelationshipService.handleUserRelationship(userProfileResponseDTO,UUIDuserId);

    }

    private UserRelationship handleReverseUserRelationship(UserProfileResponseDTO userProfileResponseDTO, UUID UUIDuserId) {
        return userRelationshipService.handleReverseUserRelationship(userProfileResponseDTO,UUIDuserId);
    }

    private ContactResponseDTO createContactResponseDTO(Contacts contact, UserRelationship relationship, UserRelationship reverseRelationship, UserProfileResponseDTO userProfileResponseDTO) {
        ContactResponseDTO dto = new ContactResponseDTO(
                ContactsDTO.builder()
                        .id(contact.getId())
                        .userId(contact.getUserId())
                        .userContactId(contact.getUserContactId())
                        .userContactName(contact.getUserContactName())
                        .relatedUserHasAddedUser(relationship != null ? relationship.isRelatedUserHasAddedUser() : reverseRelationship.isUserHasAddedRelatedUser())
                        .userHasAddedRelatedUser(relationship != null ? relationship.isUserHasAddedRelatedUser() : reverseRelationship.isUserHasAddedRelatedUser())
                        .build(),
                null,
                null
        );
        dto.setUserProfileResponseDTO(userProfileResponseDTO);
        dto.getUserProfileResponseDTO().setUserKey(UserKeyResponseDTO.builder()
                .iv(userProfileResponseDTO.getUserKey().getIv())
                .salt(userProfileResponseDTO.getUserKey().getSalt())
                .publicKey(userProfileResponseDTO.getUserKey().getPublicKey()).build());
        return dto;
    }




    @Transactional(readOnly = true)
    @Async("taskExecutor")
    public CompletableFuture<List<ContactResponseDTO>> getContactList(String tokenUserId) {
        UUID userId = UUID.fromString(tokenUserId);
        List<Invitation> invitations = this.invitationService.findInvitationByInviterUserIdOrderByContactName(userId);
        List<ContactWithRelationshipDTO> dto1 = contactsRepository.findContactsAndRelationshipsByUserId(userId);

        Map<UUID, ContactWithRelationshipDTO> contactMap = dto1.stream()
                .collect(Collectors.toMap(
                        ContactWithRelationshipDTO::getUserContactId,
                        Function.identity(),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        List<UUID> orderedIds = new ArrayList<>(contactMap.keySet());

        List<ContactResponseDTO> userResponseDTOS = userManager.getUsers(orderedIds);

        Map<UUID, ContactResponseDTO> responseMap = userResponseDTOS.stream()
                .collect(Collectors.toMap(
                        response -> response.getUserProfileResponseDTO().getId(),
                        Function.identity()
                ));

        List<ContactResponseDTO> orderedResults = new ArrayList<>();

        for (UUID orderedId : orderedIds) {
            ContactResponseDTO userResponse = responseMap.get(orderedId);
            if (userResponse != null) {
                ContactWithRelationshipDTO correspondingContact = contactMap.get(orderedId);

                // Privacy logic
                if (userResponse.getUserProfileResponseDTO().getImagee() != null) {
                    if (userResponse.getUserProfileResponseDTO().getPrivacySettings().getProfilePhotoVisibility() == VisibilityOption.NOBODY ||
                            (userResponse.getUserProfileResponseDTO().getPrivacySettings().getProfilePhotoVisibility() == VisibilityOption.MY_CONTACTS &&
                                    !correspondingContact.getRelatedUserHasAddedUser())) {
                        userResponse.getUserProfileResponseDTO().setImagee(null);
                    }
                }

                if (correspondingContact != null) {
                    ContactsDTO contactsDTO = ContactsDTO.builder()
                            .id(correspondingContact.getId())
                            .userId(correspondingContact.getUserId())
                            .userContactId(correspondingContact.getUserContactId())
                            .userContactName(correspondingContact.getUserContactName())
                            .userHasAddedRelatedUser(correspondingContact.getUserHasAddedRelatedUser())
                            .relatedUserHasAddedUser(correspondingContact.getRelatedUserHasAddedUser())
                            .build();
                    userResponse.setContactsDTO(contactsDTO);
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
    public CompletableFuture<List<ContactResponseDTO>>  getContactInformationOfExistingChats(
           ContactInformationOfExistingChatsRequestDTO contactInformationOfExistingChatsRequestDTO) {

        List<UserRelationship> relationships = userRelationshipService.getContactInformationOfExistingChats(contactInformationOfExistingChatsRequestDTO);

        List<Contacts> contacts = contactsRepository.findContactsForUser(
                contactInformationOfExistingChatsRequestDTO.getUserId(), contactInformationOfExistingChatsRequestDTO.getUserContactIds());

        Map<UUID, Contacts> contactMap = contacts.stream()
                .collect(Collectors.toMap(
                        Contacts::getUserContactId,
                        Function.identity(),
                        (existing, replacement) -> existing));

        Map<UUID, ContactWithRelationshipDTO> contactDTOMap = new HashMap<>();
        for (UserRelationship ur : relationships) {
            UUID relatedUserId = ur.getUserId().equals(contactInformationOfExistingChatsRequestDTO.getUserId()) ?
                    ur.getRelatedUserId() : ur.getUserId();

            Contacts contact = contactMap.get(relatedUserId);

            String userContactName = contact != null ? contact.getUserContactName() : null;

            boolean userHasAdded = ur.getUserId().equals(contactInformationOfExistingChatsRequestDTO.getUserId()) ?
                    ur.isUserHasAddedRelatedUser() : ur.isRelatedUserHasAddedUser();

            boolean relatedHasAdded = ur.getUserId().equals(contactInformationOfExistingChatsRequestDTO.getUserId()) ?
                    ur.isRelatedUserHasAddedUser() : ur.isUserHasAddedRelatedUser();

            ContactWithRelationshipDTO dto = new ContactWithRelationshipDTO();
            dto.setUserId(contactInformationOfExistingChatsRequestDTO.getUserId());
            dto.setUserContactId(relatedUserId);
            dto.setUserContactName(userContactName);
            dto.setUserHasAddedRelatedUser(userHasAdded);
            dto.setRelatedUserHasAddedUser(relatedHasAdded);
            dto.setId(contact != null ? contact.getId() : null);

            contactDTOMap.put(relatedUserId, dto);
        }

        List<ContactResponseDTO> userResponseDTOS = userManager.getUsers(new ArrayList<>(contactDTOMap.keySet()));

        List<ContactResponseDTO> dto = userResponseDTOS.stream()
                .map(user -> {
                    ContactWithRelationshipDTO contact = contactDTOMap.get(user.getUserProfileResponseDTO().getId());
                    String image = getImage(user, contact);
                    return ContactResponseDTO.builder()
                            .userProfileResponseDTO(UserProfileResponseDTO.builder()
                                    .id(user.getUserProfileResponseDTO().getId())
                                    .email(user.getUserProfileResponseDTO().getEmail())
                                    .about(user.getUserProfileResponseDTO().getAbout())
                                    .imagee(image)
                                    .firstName(user.getUserProfileResponseDTO().getFirstName())
                                    .lastName(user.getUserProfileResponseDTO().getLastName())
                                    .privacySettings(PrivacySettingsResponseDTO.builder()
                                            .id(user.getUserProfileResponseDTO().getPrivacySettings().getId())
                                            .onlineStatusVisibility(user.getUserProfileResponseDTO().getPrivacySettings().getOnlineStatusVisibility())
                                            .profilePhotoVisibility(user.getUserProfileResponseDTO().getPrivacySettings().getProfilePhotoVisibility())
                                            .lastSeenVisibility(user.getUserProfileResponseDTO().getPrivacySettings().getLastSeenVisibility())
                                            .aboutVisibility(user.getUserProfileResponseDTO().getPrivacySettings().getAboutVisibility())
                                            .readReceipts(user.getUserProfileResponseDTO().getPrivacySettings().isReadReceipts())
                                            .build())
                                    .userKey(UserKeyResponseDTO.builder()
                                            .iv(user.getUserProfileResponseDTO().getUserKey().getIv())
                                            .publicKey(user.getUserProfileResponseDTO().getUserKey().getPublicKey())
                                            .encryptedPrivateKey(user.getUserProfileResponseDTO().getUserKey().getEncryptedPrivateKey())
                                            .salt(user.getUserProfileResponseDTO().getUserKey().getSalt())
                                            .build())
                                    .build())
                            .contactsDTO(ContactsDTO.builder()
                                    .userContactName(contact.getUserContactName())
                                    .id(contact.getId())
                                    .userHasAddedRelatedUser(contact.getUserHasAddedRelatedUser())
                                    .relatedUserHasAddedUser(contact.getRelatedUserHasAddedUser())
                                    .userContactId(contact.getUserContactId())
                                    .userId(contact.getUserId())
                                    .build())
                            .build();
                })
                .collect(Collectors.toList());
        return CompletableFuture.completedFuture(dto);
    }



    private static String getImage(ContactResponseDTO user, ContactWithRelationshipDTO contact) {

        String image = null;
        if(user.getUserProfileResponseDTO().getImagee() != null) {
            if(user.getUserProfileResponseDTO().getPrivacySettings().getProfilePhotoVisibility() == VisibilityOption.NOBODY ||
                    (user.getUserProfileResponseDTO().getPrivacySettings().getProfilePhotoVisibility() == VisibilityOption.MY_CONTACTS && !contact.getRelatedUserHasAddedUser())) {
                image = null;
            } else {
                image = user.getUserProfileResponseDTO().getImagee();
            }
        }
        return image;
    }
    @Async("taskExecutor")
    public CompletableFuture<ContactResponseDTO>  getContactInformationOfSingleChat(ContactInformationOfExistingChatRequestDTO contactInformationOfExistingChatRequestDTO) {
        System.out.println("contactInformationOfExistingChatRequestDTO > " + contactInformationOfExistingChatRequestDTO.toString());
        Optional<ContactWithRelationshipDTO> optionalContact = contactsRepository.findContactWithRelationship(contactInformationOfExistingChatRequestDTO.getUserId(), contactInformationOfExistingChatRequestDTO.getUserContactId());
        ContactWithRelationshipDTO contact;
        UserProfileResponseDTO userProfileResponseDTO = this.userManager.getUserById(contactInformationOfExistingChatRequestDTO.getUserContactId());
        System.out.println("userProfileResponseDTO > " + userProfileResponseDTO);
        if (optionalContact.isPresent()) {
            contact = optionalContact.get();
            System.out.println("CONTACT > " + contact);
            if (contact.getUserId().equals(contactInformationOfExistingChatRequestDTO.getUserId())) {
                return CompletableFuture.completedFuture(ContactResponseDTO.builder()
                        .userProfileResponseDTO(userProfileResponseDTO)
                        .contactsDTO(ContactsDTO.builder()
                                .userContactName(contact.getUserContactName())
                                .id(contact.getId())
                                .userHasAddedRelatedUser(contact.getUserHasAddedRelatedUser())
                                .relatedUserHasAddedUser(contact.getRelatedUserHasAddedUser())
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
                                .userHasAddedRelatedUser(contact.getRelatedUserHasAddedUser())
                                .relatedUserHasAddedUser(contact.getUserHasAddedRelatedUser())
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
                            .userContactId(contactInformationOfExistingChatRequestDTO.getUserContactId())
                            .userId(contactInformationOfExistingChatRequestDTO.getUserId())
                            .build())
                    .build());
        }
    }


    private ContactResponseDTO convertInvitationToContact(Invitation invitation) {
        InvitationResponseDTO invitationResponseDTO = IInvitationMapper.INSTANCE.toInvitationResponseDTO(invitation);
        return ContactResponseDTO.builder()
                .invitationResponseDTO(invitationResponseDTO)
                .build();
    }
    @Transactional
    public void checkUsersWhoInvited(UserProfileResponseDTO userProfile) {
        List<Invitation> invitations = invitationService.getInvitations(userProfile.getEmail());

        invitations.forEach(invitation ->  {
            UserRelationship userRelationship = userRelationshipService.saveUserRelationship(invitation.getInviterUserId(), userProfile, true);
            ContactResponseDTO contactResponseDTO = new ContactResponseDTO();
            Contacts contact = saveContacts(invitation, userProfile.getId());
            ContactsDTO contactsDTO = getContactsDTO(contact, userRelationship);

            contactResponseDTO.setContactsDTO(contactsDTO);
            contactResponseDTO.setUserProfileResponseDTO(userProfile);
            contactResponseDTO.setInvitationResponseDTO(null);
            invitation.setDeleted(true);
            invitationService.saveInvitation(invitation);
            rabbitMQProducer.publishInvitedUserJoined(contactResponseDTO,contact.getUserId().toString());
        });
    }

    private ContactsDTO getContactsDTO(Contacts contact, UserRelationship userRelationship) {
        ContactsDTO contactsDTO = new ContactsDTO();
        contactsDTO.setId(contact.getId());
        contactsDTO.setUserId(contact.getUserId());
        contactsDTO.setUserContactId(contact.getUserContactId());
        contactsDTO.setUserContactName(contact.getUserContactName());
        contactsDTO.setUserHasAddedRelatedUser(userRelationship.isUserHasAddedRelatedUser());
        contactsDTO.setRelatedUserHasAddedUser(userRelationship.isRelatedUserHasAddedUser());
        return contactsDTO;
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

    public void sendUpdatedUserProfile(UpdatedProfilePhotoRequestDTO dto) {

        UserProfileResponseDTO owner = userManager.getUserById(dto.getUserId());

        List<UserRelationship> rels =
                userRelationshipService.findByUserIdOrRelatedUserId(dto.getUserId());

        List<UUID> targets =
                userRelationshipService.filterTargetsForProfileUpdate(dto.getUserId(), rels, owner);

        for (UUID t : targets) {
            rabbitMQProducer.publishProfile(dto, t);
        }
    }

    public void sendUpdatedUserPrivacy(UpdatePrivacySettingsRequestDTO dto) {

        UserProfileResponseDTO owner = userManager.getUserById(dto.getId());

        List<UserRelationship> rels =
                userRelationshipService.findByUserIdOrRelatedUserId(dto.getId());

        List<UUID> targets =
                userRelationshipService.filterTargetsForProfileUpdate(dto.getId(), rels, owner);

        for (UUID t : targets) {
            rabbitMQProducer.publishPrivacy(dto, t);
        }
    }
}
