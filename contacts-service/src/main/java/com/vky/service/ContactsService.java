package com.vky.service;

import com.vky.dto.request.ContactInformationOfExistingChatRequestDTO;
import com.vky.dto.request.ContactInformationOfExistingChatsRequestDTO;
import com.vky.dto.request.ContactRequestDTO;
import com.vky.dto.request.FeignClientUserProfileRequestDTO;
import com.vky.dto.response.*;
import com.vky.exception.ContactNotFoundException;
import com.vky.exception.InvitationAlreadyExistsException;
import com.vky.manager.IUserManager;
import com.vky.mapper.IInvitationMapper;
import com.vky.repository.ContactWithRelationshipDTO;
import com.vky.repository.IContactsRepository;
import com.vky.repository.IUserRelationshipRepository;
import com.vky.repository.entity.Contacts;
import com.vky.repository.entity.Invitation;
import com.vky.repository.entity.UserRelationship;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

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
    private final WebClient webClient;

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

    public DeleteContactResponseDTO deleteContact(UUID id) {
        Contacts contact = contactsRepository.findById(id)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found for id: " + id));


        contact.setDeleted(true);
        contactsRepository.save(contact);

        Invitation invitation = invitationService.findInvitationAndDelete(contact.getUserId(), contact.getUserContactEmail());

        DeleteContactResponseDTO dto = new DeleteContactResponseDTO(
                invitation != null ? invitation.getId() : null,
                invitation != null ? invitation.getInviteeEmail() : null,
                invitation != null ? invitation.getContactName() : null,
                invitation != null ? invitation.getInviterUserId() : null,
                contact.getId(),
                invitation != null ? invitation.isInvited() : null
        );

        messagingTemplate.convertAndSendToUser(
                contact.getUserId().toString(),
                "queue/delete/contact",
                dto
        );

        return dto;
    }

    public void sendUpdatedPrivacySettings(UserProfileResponseDTO userProfileResponseDTO) {
        List<UserRelationship> userRelationships = userRelationshipRepository.findByUserIdOrRelatedUserId(userProfileResponseDTO.getId());
        Set<String> alreadyMessagedUsers = new HashSet<>();
        userRelationships.forEach(userRelationship -> {
            if (userRelationship.getUserId().equals(userProfileResponseDTO.getId())) {
                if (!alreadyMessagedUsers.contains(userRelationship.getRelatedUserId().toString())) {
                    alreadyMessagedUsers.add(userRelationship.getRelatedUserId().toString());
                    messagingTemplate.convertAndSendToUser(userRelationship.getRelatedUserId().toString(), "/queue/update-privacy-response", userProfileResponseDTO);
                }

            } else {
                if (!alreadyMessagedUsers.contains(userRelationship.getUserId().toString())) {
                    alreadyMessagedUsers.add(userRelationship.getUserId().toString());
                    messagingTemplate.convertAndSendToUser(userRelationship.getUserId().toString(), "/queue/update-privacy-response", userProfileResponseDTO);
                }

            }
        });
    }


    public record AddInvitationResponseDTO(UUID id, UUID userContactId, String userContactEmail, String userContactName,
                                           boolean isInvited, String about, String image, String name) {
    }


    public void addContact(ContactRequestDTO contactRequestDTO) {
        UserProfileResponseDTO userProfileResponseDTO = userManager.getUserByEmail(contactRequestDTO.userContactEmail());
        if (userProfileResponseDTO == null) {
            handleInvitationProcess(contactRequestDTO);
        } else {
            handleExistingContactProcess(contactRequestDTO, userProfileResponseDTO);
        }
    }

    private void handleInvitationProcess(ContactRequestDTO contactRequestDTO) {
        if (invitationService.isExistsInvitation(contactRequestDTO.userId(), contactRequestDTO.userContactEmail())) {
            throw new InvitationAlreadyExistsException("Invitation already exists for user with email: " + contactRequestDTO.userContactEmail());
        } else {
            Invitation invitation = invitationService.addInvitation(contactRequestDTO);
            AddInvitationResponseDTO addInvitationResponseDTO = new AddInvitationResponseDTO(invitation.getId(), null, invitation.getInviteeEmail(), invitation.getContactName(), invitation.isInvited(), null, null, null);
            messagingTemplate.convertAndSendToUser(invitation.getInviterUserId().toString(), "/queue/add-invitation", addInvitationResponseDTO);
        }
    }

    private void handleExistingContactProcess(ContactRequestDTO contactRequestDTO, UserProfileResponseDTO userProfileResponseDTO) {
        Optional<Contacts> existingContactOpt = contactsRepository.findContactsByUserContactEmailAndUserId(
                userProfileResponseDTO.getEmail(),
                contactRequestDTO.userId()
        );

        Contacts contact = existingContactOpt.map(existingContact -> {
            if (!existingContact.isDeleted()) {
                throw new InvitationAlreadyExistsException(
                        "Contact already exists and is marked as deleted for user with email: " + contactRequestDTO.userContactEmail()
                );
            }
            existingContact.setDeleted(false);
            existingContact.setUserContactName(contactRequestDTO.userContactName());
            return contactsRepository.save(existingContact);
        }).orElseGet(() -> contactsRepository.save(
                Contacts.builder()
                        .userContactEmail(contactRequestDTO.userContactEmail())
                        .userContactName(contactRequestDTO.userContactName())
                        .userId(contactRequestDTO.userId())
                        .userContactId(userProfileResponseDTO.getId())
                        .build()
        ));
        UserRelationship reverseRelationship = handleReverseUserRelationship(contactRequestDTO, userProfileResponseDTO);
        UserRelationship relationship = (reverseRelationship == null) ? handleUserRelationship(contactRequestDTO, userProfileResponseDTO) : null;

        ContactResponseDTO contactResponseDTO = createContactResponseDTO(contact, relationship, reverseRelationship, userProfileResponseDTO);
        messagingTemplate.convertAndSendToUser(contact.getUserId().toString(), "/queue/add-contact", contactResponseDTO);
    }

    private UserRelationship handleUserRelationship(ContactRequestDTO contactRequestDTO, UserProfileResponseDTO userProfileResponseDTO) {
        Optional<UserRelationship> relationshipOpt = userRelationshipRepository.findByUserIdAndRelatedUserId(
                contactRequestDTO.userId(),
                userProfileResponseDTO.getId()
        );

        if (relationshipOpt.isPresent()) {
            UserRelationship relationship = relationshipOpt.get();
            relationship.setUserHasAddedRelatedUser(true);
            return relationship;
        } else {
            UserRelationship newRelationship = new UserRelationship();
            newRelationship.setUserId(contactRequestDTO.userId());
            newRelationship.setRelatedUserId(userProfileResponseDTO.getId());
            newRelationship.setUserHasAddedRelatedUser(true);
            newRelationship.setRelatedUserHasAddedUser(false);
            return userRelationshipRepository.save(newRelationship);
        }
    }

    private UserRelationship handleReverseUserRelationship(ContactRequestDTO contactRequestDTO, UserProfileResponseDTO userProfileResponseDTO) {
        Optional<UserRelationship> reverseRelationshipOpt = userRelationshipRepository.findByUserIdAndRelatedUserId(
                userProfileResponseDTO.getId(),
                contactRequestDTO.userId()
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
        return dto;
    }


    //    public List<FeignClientUserProfileResponseDTO> getContactList(UUID userId) {
//        List<Invitation> invitations = this.invitationService.findInvitationByInviterUserIdOrderByContactName(userId);
//        List<ContactWithRelationshipDTO> dto1 = contactsRepository.findContactsAndRelationshipsByUserId(userId);
//
//        Map<UUID, ContactWithRelationshipDTO> contactMap = dto1.stream()
//                .collect(Collectors.toMap(ContactWithRelationshipDTO::getUserContactId, Function.identity()));
//
//        List<FeignClientUserProfileRequestDTO> contactList = dto1.stream()
//                .map(contact -> FeignClientUserProfileRequestDTO.builder()
//                        .id(contact.getUserContactId())
//                        .userContactName(contact.getUserContactName())
//                        .userProfileResponseDTO(null)  // Profil verileri sonra doldurulacak
//                        .build())
//                .collect(Collectors.toList());
//
//        List<FeignClientUserProfileResponseDTO> userResponseDTOS = this.userManager.getUserList(contactList);
//
//        for (FeignClientUserProfileResponseDTO userResponse : userResponseDTOS) {
//            UUID userContactId = userResponse.getUserProfileResponseDTO().getId();
//
//            ContactWithRelationshipDTO correspondingContact = contactMap.get(userContactId);
//
//            if (correspondingContact != null) {
//                ContactsDTO contactsDTO = ContactsDTO.builder()
//                        .id(correspondingContact.getId())
//                        .userId(correspondingContact.getUserId())
//                        .userContactId(correspondingContact.getUserContactId())
//                        .userContactName(correspondingContact.getUserContactName())
//                        .userHasAddedRelatedUser(correspondingContact.getUserHasAddedRelatedUser())
//                        .relatedUserHasAddedUser(correspondingContact.getRelatedUserHasAddedUser())
//                        .build();
//                userResponse.setContactsDTO(contactsDTO);
//            }
//        }
//
//        userResponseDTOS.sort(Comparator.comparing(
//                userResponse -> userResponse.getContactsDTO() != null ? userResponse.getContactsDTO().getUserContactName() : "")
//        );
//
//        List<FeignClientUserProfileResponseDTO> invitationResponseDTOS = invitations.stream()
//                .map(this::convertInvitationToContact)
//                .sorted(Comparator.comparing(dto -> dto.getInvitationResponseDTO().getContactName(), Comparator.nullsFirst(String::compareTo)))
//                .toList();
//
//        userResponseDTOS.addAll(invitationResponseDTOS);
//
//        return userResponseDTOS;
//    }

    @Async("taskExecutor")
    public CompletableFuture<List<FeignClientUserProfileResponseDTO>> getUsersOfContacts(List<FeignClientUserProfileRequestDTO> userRequestDTOList) {
        return webClient.post()
                .uri("/get-users-of-contacts")
                .body(Mono.just(userRequestDTOList), FeignClientUserProfileRequestDTO.class)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<FeignClientUserProfileResponseDTO>>() {
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    System.err.println("Error fetching participant profiles: " + ex.getMessage());
                    return Mono.just(Collections.emptyList());
                })
                .toFuture();
    }

    @Async("taskExecutor")
    public CompletableFuture<List<FeignClientUserProfileResponseDTO>> getContactListAsync(UUID userId) {
        List<Invitation> invitations = this.invitationService.findInvitationByInviterUserIdOrderByContactName(userId);
        List<ContactWithRelationshipDTO> dto1 = contactsRepository.findContactsAndRelationshipsByUserId(userId);

        Map<UUID, ContactWithRelationshipDTO> contactMap = dto1.stream()
                .collect(Collectors.toMap(ContactWithRelationshipDTO::getUserContactId, Function.identity()));

        List<FeignClientUserProfileRequestDTO> contactList = dto1.stream()
                .map(contact -> FeignClientUserProfileRequestDTO.builder()
                        .id(contact.getUserContactId())
                        .userContactName(contact.getUserContactName())
                        .userProfileResponseDTO(null)  // Profil verileri sonra doldurulacak
                        .build())
                .toList();

        // Asenkron olarak userManager.getUserListAsync çağrısı
        return getUsersOfContacts(contactList)
                .thenApply(userResponseDTOS -> {
                    // Kullanıcı verilerini işleyip ContactsDTO ile eşleştiriyoruz
                    for (FeignClientUserProfileResponseDTO userResponse : userResponseDTOS) {
                        UUID userContactId = userResponse.getUserProfileResponseDTO().getId();

                        ContactWithRelationshipDTO correspondingContact = contactMap.get(userContactId);

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
                    }

                    // Kullanıcı verilerini isme göre sıralıyoruz
                    userResponseDTOS.sort(Comparator.comparing(
                            userResponse -> userResponse.getContactsDTO() != null ? userResponse.getContactsDTO().getUserContactName() : "")
                    );

                    // Davetleri işleyip sonuç listesine ekliyoruz
                    List<FeignClientUserProfileResponseDTO> invitationResponseDTOS = invitations.stream()
                            .map(this::convertInvitationToContact)
                            .sorted(Comparator.comparing(dto -> dto.getInvitationResponseDTO().getContactName(), Comparator.nullsFirst(String::compareTo)))
                            .toList();

                    userResponseDTOS.addAll(invitationResponseDTOS);

                    return userResponseDTOS;
                });
    }

    //    public List<FeignClientUserProfileResponseDTO> getContactInformationOfExistingChats(ContactInformationOfExistingChatsRequestDTO requestDTO) {
//        List<ContactWithRelationshipDTO> contacts = contactsRepository.findContactsWithRelationships(requestDTO.getUserId(), requestDTO.getUserContactIds());
//
//        Map<UUID, ContactWithRelationshipDTO> contactNameMap = contacts.stream()
//                .filter(contact -> !contact.getUserContactId().equals(contact.getUserId()) || contact.getUserId().equals(requestDTO.getUserId()))
//                .collect(Collectors.toMap(
//                        contact -> contact.getUserId().equals(requestDTO.getUserId()) ? contact.getUserContactId() : contact.getUserId(),
//                        contact -> {
//                            if (contact.getUserContactId().equals(requestDTO.getUserId())) {
//                                UUID tempUserId = contact.getUserId();
//                                contact.setUserId(contact.getUserContactId());
//                                contact.setUserContactId(tempUserId);
//                                contact.setUserContactName(null);
//                            }
//                            return contact;
//                        },
//                        (existing, replacement) -> existing.getUserContactName() != null && !existing.getUserContactName().isEmpty() ? existing : replacement
//                ));
//        List<UUID> missingIds = requestDTO.getUserContactIds().stream()
//                .filter(id -> !contactNameMap.containsKey(id))
//                .toList();
//        ;
//        missingIds.forEach(id -> contactNameMap.put(id, new ContactWithRelationshipDTO(null, requestDTO.getUserId(), id, null, false, false)));
//        List<ContactWithRelationshipDTO> contacts1 = new ArrayList<>(contactNameMap.values());
//
//        List<FeignClientUserProfileResponseDTO> userResponseDTOS = this.userManager.getUserListt(contacts1);
//        userResponseDTOS = userResponseDTOS.stream()
//                .map(user -> {
//                    ContactWithRelationshipDTO contact = contactNameMap.get(user.getUserProfileResponseDTO().getId());
//                    return FeignClientUserProfileResponseDTO.builder()
//                            .userProfileResponseDTO(UserProfileResponseDTO.builder()
//                                    .id(user.getUserProfileResponseDTO().getId())
//                                    .email(user.getUserProfileResponseDTO().getEmail())
//                                    .about(user.getUserProfileResponseDTO().getAbout())
//                                    .imagee(user.getUserProfileResponseDTO().getImagee())
//                                    .firstName(user.getUserProfileResponseDTO().getFirstName())
//                                    .lastName(user.getUserProfileResponseDTO().getLastName())
//                                    .privacySettings(PrivacySettingsResponseDTO.builder()
//                                            .id(user.getUserProfileResponseDTO().getPrivacySettings().getId())
//                                            .onlineStatusVisibility(user.getUserProfileResponseDTO().getPrivacySettings().getOnlineStatusVisibility())
//                                            .profilePhotoVisibility(user.getUserProfileResponseDTO().getPrivacySettings().getProfilePhotoVisibility())
//                                            .lastSeenVisibility(user.getUserProfileResponseDTO().getPrivacySettings().getLastSeenVisibility())
//                                            .aboutVisibility(user.getUserProfileResponseDTO().getPrivacySettings().getAboutVisibility())
//                                            .readReceipts(user.getUserProfileResponseDTO().getPrivacySettings().isReadReceipts()).build()).build())
//                            .contactsDTO(ContactsDTO.builder().userContactName(contact.getUserContactName())
//                                    .id(contact.getId())
//                                    .userHasAddedRelatedUser(contact.getUserHasAddedRelatedUser())
//                                    .relatedUserHasAddedUser(contact.getRelatedUserHasAddedUser())
//                                    .userContactId(contact.getUserContactId())
//                                    .userId(contact.getUserId()).build())
//                            .build();
//                })
//                .toList();
//        userResponseDTOS.forEach(userResponseDTO -> {
//        });
//        return userResponseDTOS;
//    }
    @Async("taskExecutor")
    public CompletableFuture<List<FeignClientUserProfileResponseDTO>> getUsersOfChats(List<ContactWithRelationshipDTO> contacts) {
        return webClient.post()
                .uri("/get-users-of-chats")
                .body(Mono.just(contacts), FeignClientUserProfileResponseDTO.class)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<FeignClientUserProfileResponseDTO>>() {
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    // Log error and return empty list in case of error
                    System.err.println("Error fetching participant profiles: " + ex.getMessage());
                    return Mono.just(Collections.emptyList());
                })
                .toFuture();
    }

    @Async("taskExecutor")
    public CompletableFuture<List<FeignClientUserProfileResponseDTO>> getContactInformationOfExistingChatsAsync(ContactInformationOfExistingChatsRequestDTO requestDTO) {

        // Repository sorgusunu asenkron hale getirdik
        CompletableFuture<List<ContactWithRelationshipDTO>> contactsFuture = CompletableFuture.supplyAsync(() ->
                contactsRepository.findContactsWithRelationships(requestDTO.getUserId(), requestDTO.getUserContactIds())
        );

        // Sorgu tamamlandıktan sonra sonucu işleyerek devam ediyoruz
        return contactsFuture.thenCompose(contacts -> {
            Map<UUID, ContactWithRelationshipDTO> contactNameMap = contacts.stream()
                    .filter(contact -> !contact.getUserContactId().equals(contact.getUserId()) || contact.getUserId().equals(requestDTO.getUserId()))
                    .collect(Collectors.toMap(
                            contact -> contact.getUserId().equals(requestDTO.getUserId()) ? contact.getUserContactId() : contact.getUserId(),
                            contact -> {
                                if (contact.getUserContactId().equals(requestDTO.getUserId())) {
                                    UUID tempUserId = contact.getUserId();
                                    contact.setUserId(contact.getUserContactId());
                                    contact.setUserContactId(tempUserId);
                                    contact.setUserContactName(null);
                                }
                                return contact;
                            },
                            (existing, replacement) -> existing.getUserContactName() != null && !existing.getUserContactName().isEmpty() ? existing : replacement
                    ));

            List<UUID> missingIds = requestDTO.getUserContactIds().stream()
                    .filter(id -> !contactNameMap.containsKey(id))
                    .toList();

            missingIds.forEach(id -> contactNameMap.put(id, new ContactWithRelationshipDTO(null, requestDTO.getUserId(), id, null, false, false)));
            List<ContactWithRelationshipDTO> contacts1 = new ArrayList<>(contactNameMap.values());

            // Asenkron olarak userManager.getUserListAsync çağrısı
            return getUsersOfChats(contacts1)
                    .thenApply(userResponseDTOS -> userResponseDTOS.stream()
                            .map(user -> {
                                ContactWithRelationshipDTO contact = contactNameMap.get(user.getUserProfileResponseDTO().getId());
                                return FeignClientUserProfileResponseDTO.builder()
                                        .userProfileResponseDTO(UserProfileResponseDTO.builder()
                                                .id(user.getUserProfileResponseDTO().getId())
                                                .email(user.getUserProfileResponseDTO().getEmail())
                                                .about(user.getUserProfileResponseDTO().getAbout())
                                                .imagee(user.getUserProfileResponseDTO().getImagee())
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
                            .toList());
        });
    }

    public FeignClientUserProfileResponseDTO getContactInformationOfSingleChat(ContactInformationOfExistingChatRequestDTO contactInformationOfExistingChatRequestDTO) {
        System.out.println("contactInformationOfExistingChatRequestDTO > " + contactInformationOfExistingChatRequestDTO.toString());
        Optional<ContactWithRelationshipDTO> optionalContact = contactsRepository.findContactWithRelationship(contactInformationOfExistingChatRequestDTO.getUserId(), contactInformationOfExistingChatRequestDTO.getUserContactId());
        ContactWithRelationshipDTO contact;
        UserProfileResponseDTO userProfileResponseDTO = this.userManager.getFeignUserById(contactInformationOfExistingChatRequestDTO.getUserContactId());
        System.out.println("userProfileResponseDTO > " + userProfileResponseDTO);
        if (optionalContact.isPresent()) {
            contact = optionalContact.get();
            System.out.println("CONTACT > " + contact);
            if (contact.getUserId().equals(contactInformationOfExistingChatRequestDTO.getUserId())) {
                return FeignClientUserProfileResponseDTO.builder()
                        .userProfileResponseDTO(userProfileResponseDTO)
                        .contactsDTO(ContactsDTO.builder()
                                .userContactName(contact.getUserContactName())
                                .id(contact.getId())
                                .userHasAddedRelatedUser(contact.getUserHasAddedRelatedUser())
                                .relatedUserHasAddedUser(contact.getRelatedUserHasAddedUser())
                                .userContactId(contact.getUserContactId())
                                .userId(contact.getUserId())
                                .build())
                        .build();
            } else {

                return FeignClientUserProfileResponseDTO.builder()
                        .userProfileResponseDTO(userProfileResponseDTO)
                        .contactsDTO(ContactsDTO.builder()
                                .userContactName(null)
                                .id(contact.getId())
                                .userHasAddedRelatedUser(contact.getRelatedUserHasAddedUser())
                                .relatedUserHasAddedUser(contact.getUserHasAddedRelatedUser())
                                .userContactId(contact.getUserId())
                                .userId(contact.getUserContactId())
                                .build())
                        .build();
            }
        } else {
            return FeignClientUserProfileResponseDTO.builder()
                    .userProfileResponseDTO(userProfileResponseDTO)
                    .contactsDTO(ContactsDTO.builder()
                            .userContactName(null)
                            .id(null)
                            .userHasAddedRelatedUser(false)
                            .relatedUserHasAddedUser(false)
                            .userContactId(contactInformationOfExistingChatRequestDTO.getUserContactId())
                            .userId(contactInformationOfExistingChatRequestDTO.getUserId())
                            .build())
                    .build();
        }
    }


    private FeignClientUserProfileResponseDTO convertInvitationToContact(Invitation invitation) {
        InvitationResponseDTO invitationResponseDTO = IInvitationMapper.INSTANCE.toInvitationResponseDTO(invitation);
        return FeignClientUserProfileResponseDTO.builder()
                .invitationResponseDTO(invitationResponseDTO)
                .build();
    }

}
