package com.vky.service;

import com.vky.dto.request.*;
import com.vky.dto.response.*;
import com.vky.exception.ContactsServiceException;
import com.vky.exception.ErrorType;
import com.vky.manager.IUserManager;
import com.vky.mapper.IInvitationMapper;
import com.vky.repository.ContactWithRelationshipDTO;
import com.vky.repository.IContactsRepository;
import com.vky.repository.IUserRelationshipRepository;
import com.vky.repository.entity.Contacts;
import com.vky.repository.entity.Invitation;
import com.vky.repository.entity.UserRelationship;
import com.vky.repository.entity.enums.VisibilityOption;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate messagingTemplate;
    private final IUserRelationshipRepository userRelationshipRepository;

    public boolean isExists(String invitedUserEmail, UUID invitedByUserId) {
        Optional<Contacts> contactOptional = contactsRepository.findContactsByUserContactEmailAndUserId(invitedUserEmail, invitedByUserId);

        if (contactOptional.isPresent()) {
            Contacts contact = contactOptional.get();
            if (contact.isDeleted()) {
                contact.setDeleted(false);
                contactsRepository.save(contact);
                return false;
            } else {
                throw new IllegalStateException("Contact already exists and is active.");
            }
        }
        return false;
    }

    public void saveRegisterUserContact(Invitation invitation, UUID contactId) {
        Contacts contact = contactsRepository.save(Contacts.builder()
                .userId(invitation.getInviterUserId())
                .userContactName(invitation.getContactName())
                .userContactId(contactId)
                .userContactEmail(invitation.getInviteeEmail()).build());
        messagingTemplate.convertAndSendToUser(contact.getUserId().toString(), "/topic/invitation", contact);
    }
    @Transactional
    public DeleteContactResponseDTO deleteContact(UUID id, String userId) {
        Contacts contact = contactsRepository.findById(id)
                .orElseThrow(() -> new ContactsServiceException(ErrorType.CONTACT_NOT_FOUND,id.toString()));

        contact.setDeleted(true);
        contact.setUserContactName(null);
        contactsRepository.save(contact);

        UserRelationship userRelationship = userRelationshipRepository.findRelationshipBetweenUsers(UUID.fromString(userId), contact.getUserContactId())
                .orElseThrow(() -> new ContactsServiceException(ErrorType.CONTACT_NOT_FOUND,id.toString()));
        if(userRelationship.getUserId().equals(contact.getUserId())) {
            userRelationship.setUserHasAddedRelatedUser(false);
        } else {
            userRelationship.setRelatedUserHasAddedUser(false);
        }
        userRelationshipRepository.save(userRelationship);
        return new DeleteContactResponseDTO();
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

    @Async("taskExecutor")
    public void addContact(ContactRequestDTO contactRequestDTO, String userId) {
        UserProfileResponseDTO userProfileResponseDTO = userManager.getUserByEmail(contactRequestDTO.getUserContactEmail());
        if (userProfileResponseDTO == null) {
            handleInvitationProcess(contactRequestDTO,userId);
        } else {
            handleExistingContactProcess(contactRequestDTO, userProfileResponseDTO, userId);
        }
    }

    private void handleInvitationProcess(ContactRequestDTO contactRequestDTO, String userId) {
        if (invitationService.isExistsInvitation(UUID.fromString(userId), contactRequestDTO.getUserContactEmail())) {
            throw new ContactsServiceException(ErrorType.INVITATION_ALREADY,contactRequestDTO.getUserContactEmail());
        } else {
            Invitation invitation = invitationService.addInvitation(contactRequestDTO, userId);
            FeignClientUserProfileResponseDTO dto = new FeignClientUserProfileResponseDTO();
            dto.setInvitationResponseDTO(new InvitationResponseDTO(invitation.getId(),invitation.isInvited(),invitation.getContactName(),invitation.getInviterUserId()));
            messagingTemplate.convertAndSendToUser(invitation.getInviterUserId().toString(), "/queue/add-invitation", dto);
        }
    }

    private void handleExistingContactProcess(ContactRequestDTO contactRequestDTO, UserProfileResponseDTO userProfileResponseDTO, String userId) {
        UUID UUIDuserId = UUID.fromString(userId);
        Optional<Contacts> existingContactOpt = contactsRepository.findContactsByUserContactEmailAndUserId(
                userProfileResponseDTO.getEmail(),
                UUIDuserId
        );

        Contacts contact = existingContactOpt.map(existingContact -> {
            if (!existingContact.isDeleted()) {
                throw new ContactsServiceException(
                        ErrorType.CONTACT_ALREADY,contactRequestDTO.getUserContactEmail()
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
        messagingTemplate.convertAndSendToUser(contact.getUserId().toString(), "/queue/add-contact", contactResponseDTO);
        userProfileResponseDTO.setImagee(contactRequestDTO.getImagee());
        messagingTemplate.convertAndSendToUser(contact.getUserContactId().toString(), "/queue/add-contact-user", contactResponseDTO);
    }

    private UserRelationship handleUserRelationship(UserProfileResponseDTO userProfileResponseDTO, UUID UUIDuserId) {
        Optional<UserRelationship> relationshipOpt = userRelationshipRepository.findByUserIdAndRelatedUserId(
                UUIDuserId,
                userProfileResponseDTO.getId()
        );

        if (relationshipOpt.isPresent()) {
            UserRelationship relationship = relationshipOpt.get();
            relationship.setUserHasAddedRelatedUser(true);
            return userRelationshipRepository.save(relationship);
        } else {
            UserRelationship newRelationship = new UserRelationship();
            newRelationship.setUserId(UUIDuserId);
            newRelationship.setRelatedUserId(userProfileResponseDTO.getId());
            newRelationship.setUserHasAddedRelatedUser(true);
            newRelationship.setRelatedUserHasAddedUser(false);
            return userRelationshipRepository.save(newRelationship);
        }
    }

    private UserRelationship handleReverseUserRelationship(UserProfileResponseDTO userProfileResponseDTO, UUID UUIDuserId) {
        Optional<UserRelationship> reverseRelationshipOpt = userRelationshipRepository.findByUserIdAndRelatedUserId(
                userProfileResponseDTO.getId(),
                UUIDuserId
        );

        if (reverseRelationshipOpt.isPresent()) {
            UserRelationship reverseRelationship = reverseRelationshipOpt.get();
            reverseRelationship.setRelatedUserHasAddedUser(true);
            return userRelationshipRepository.save(reverseRelationship);
        }
        return null;
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
    public CompletableFuture<List<FeignClientUserProfileResponseDTO>> getContactList(String tokenUserId) {
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

        List<FeignClientUserProfileResponseDTO> userResponseDTOS = userManager.getUsers(orderedIds);

        Map<UUID, FeignClientUserProfileResponseDTO> responseMap = userResponseDTOS.stream()
                .collect(Collectors.toMap(
                        response -> response.getUserProfileResponseDTO().getId(),
                        Function.identity()
                ));

        List<FeignClientUserProfileResponseDTO> orderedResults = new ArrayList<>();

        for (UUID orderedId : orderedIds) {
            FeignClientUserProfileResponseDTO userResponse = responseMap.get(orderedId);
            if (userResponse != null) {
                ContactWithRelationshipDTO correspondingContact = contactMap.get(orderedId);

                // Privacy logic
                if (userResponse.getUserProfileResponseDTO().getImagee() != null) {
                    if (userResponse.getUserProfileResponseDTO().getPrivacySettings().getProfilePhotoVisibility() == VisibilityOption.NOBODY ||
                            (userResponse.getUserProfileResponseDTO().getPrivacySettings().getProfilePhotoVisibility() == VisibilityOption.CONTACTS &&
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

        List<FeignClientUserProfileResponseDTO> invitationResponseDTOS = invitations.stream()
                .map(this::convertInvitationToContact)
                .toList();

        orderedResults.addAll(invitationResponseDTOS);

        return CompletableFuture.completedFuture(orderedResults);
    }

    @Async("taskExecutor")
    public CompletableFuture<List<FeignClientUserProfileResponseDTO>>  getContactInformationOfExistingChats(
           ContactInformationOfExistingChatsRequestDTO contactInformationOfExistingChatsRequestDTO) {

        List<UserRelationship> relationships = userRelationshipRepository.findRelationshipsForUser(
                contactInformationOfExistingChatsRequestDTO.getUserId(), contactInformationOfExistingChatsRequestDTO.getUserContactIds());
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

        List<FeignClientUserProfileResponseDTO> userResponseDTOS = userManager.getUsers(new ArrayList<>(contactDTOMap.keySet()));

        List<FeignClientUserProfileResponseDTO> dto = userResponseDTOS.stream()
                .map(user -> {
                    ContactWithRelationshipDTO contact = contactDTOMap.get(user.getUserProfileResponseDTO().getId());
                    String image = getImage(user, contact);
                    return FeignClientUserProfileResponseDTO.builder()
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



    private static String getImage(FeignClientUserProfileResponseDTO user, ContactWithRelationshipDTO contact) {

        String image = null;
        if(user.getUserProfileResponseDTO().getImagee() != null) {
            if(user.getUserProfileResponseDTO().getPrivacySettings().getProfilePhotoVisibility() == VisibilityOption.NOBODY ||
                    (user.getUserProfileResponseDTO().getPrivacySettings().getProfilePhotoVisibility() == VisibilityOption.CONTACTS && !contact.getRelatedUserHasAddedUser())) {
                image = null;
            } else {
                image = user.getUserProfileResponseDTO().getImagee();
            }
        }
        return image;
    }
    @Async("taskExecutor")
    public CompletableFuture<FeignClientUserProfileResponseDTO>  getContactInformationOfSingleChat(ContactInformationOfExistingChatRequestDTO contactInformationOfExistingChatRequestDTO) {
        System.out.println("contactInformationOfExistingChatRequestDTO > " + contactInformationOfExistingChatRequestDTO.toString());
        Optional<ContactWithRelationshipDTO> optionalContact = contactsRepository.findContactWithRelationship(contactInformationOfExistingChatRequestDTO.getUserId(), contactInformationOfExistingChatRequestDTO.getUserContactId());
        ContactWithRelationshipDTO contact;
        UserProfileResponseDTO userProfileResponseDTO = this.userManager.getFeignUserById(contactInformationOfExistingChatRequestDTO.getUserContactId());
        System.out.println("userProfileResponseDTO > " + userProfileResponseDTO);
        if (optionalContact.isPresent()) {
            contact = optionalContact.get();
            System.out.println("CONTACT > " + contact);
            if (contact.getUserId().equals(contactInformationOfExistingChatRequestDTO.getUserId())) {
                return CompletableFuture.completedFuture(FeignClientUserProfileResponseDTO.builder()
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

                return CompletableFuture.completedFuture(FeignClientUserProfileResponseDTO.builder()
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
            return CompletableFuture.completedFuture(FeignClientUserProfileResponseDTO.builder()
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


    private FeignClientUserProfileResponseDTO convertInvitationToContact(Invitation invitation) {
        InvitationResponseDTO invitationResponseDTO = IInvitationMapper.INSTANCE.toInvitationResponseDTO(invitation);
        return FeignClientUserProfileResponseDTO.builder()
                .invitationResponseDTO(invitationResponseDTO)
                .build();
    }

}
