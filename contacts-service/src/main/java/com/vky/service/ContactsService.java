package com.vky.service;

import com.vky.dto.request.ContactInformationOfExistingChatsRequestDTO;
import com.vky.dto.request.ContactRequestDTO;
import com.vky.dto.request.FeignClientUserProfileRequestDTO;
import com.vky.dto.response.*;
import com.vky.exception.ContactNotFoundException;
import com.vky.exception.InvitationAlreadyExistsException;
import com.vky.manager.IUserManager;
import com.vky.mapper.IContactsMapper;
import com.vky.mapper.IInvitationMapper;
import com.vky.repository.IContactsRepository;
import com.vky.repository.entity.Contacts;
import com.vky.repository.entity.Invitation;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactsService {
    private final IContactsRepository contactsRepository;
    private final IUserManager userManager;
    private final InvitationService invitationService;
    private final SimpMessagingTemplate messagingTemplate;

    public boolean isExists(String invitedUserEmail, UUID invitedByUserId) {
        return contactsRepository.existsContactsByUserContactEmailAndUserId(invitedUserEmail, invitedByUserId);
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
        List<UUID> contacts = contactsRepository.findUserContactsByUserIdOrUserContactId(userProfileResponseDTO.getId());
        contacts.forEach(contact -> {
            messagingTemplate.convertAndSendToUser(contact.toString(), "/queue/update-privacy-response", userProfileResponseDTO);
        });
    }


    public record AddContactResponseDTO(UUID id, UUID userContactId, String userContactEmail, String about,
                                        String image, String name, String userContactName) {
    }

    ;

    public record AddInvitationResponseDTO(UUID id, UUID userContactId, String userContactEmail, String userContactName,
                                           boolean isInvited, String about, String image, String name) {
    }

    ;

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
        if (contactsRepository.existsContactsByUserContactEmailAndUserId(userProfileResponseDTO.getEmail(), contactRequestDTO.userId())) {
            throw new InvitationAlreadyExistsException("Contact already exists for user with email: " + contactRequestDTO.userContactEmail());
        }
        Contacts contact = contactsRepository.save(Contacts.builder()
                .userContactEmail(contactRequestDTO.userContactEmail())
                .userContactName(contactRequestDTO.userContactName())
                .userId(contactRequestDTO.userId())
                .userContactId(userProfileResponseDTO.getId()).build());
        ContactResponseDTO contactResponseDTO = new ContactResponseDTO(contact.getId(), userProfileResponseDTO, contact.getUserContactName());
        messagingTemplate.convertAndSendToUser(contact.getUserId().toString(), "/queue/add-contact", contactResponseDTO);
    }

    public List<FeignClientUserProfileResponseDTO> getContactList(UUID userId) {
        List<Contacts> contacts = this.contactsRepository.findContactsByUserIdOrderByUserContactName(userId);
        List<Invitation> invitations = this.invitationService.findInvitationByInviterUserIdOrderByContactName(userId);

        List<FeignClientUserProfileRequestDTO> contactList = IContactsMapper.INSTANCE.toContactRequestList(contacts);

        List<FeignClientUserProfileResponseDTO> userResponseDTOS = this.userManager.getUserList(contactList);

        userResponseDTOS = checkReversedContactIds(userId, contacts, userResponseDTOS);

        userResponseDTOS.sort(Comparator.comparing(FeignClientUserProfileResponseDTO::getUserContactName));
        List<FeignClientUserProfileResponseDTO> invitationResponseDTOS = invitations.stream()
                .map(this::convertInvitationToContact)
                .sorted(Comparator.comparing(dto -> dto.getInvitationResponseDTO().getContactName(), Comparator.nullsFirst(String::compareTo)))
                .toList();

        userResponseDTOS.addAll(invitationResponseDTOS);
        return userResponseDTOS;
    }

    public List<FeignClientUserProfileResponseDTO> checkReversedContactIds(UUID userId, List<Contacts> contacts, List<FeignClientUserProfileResponseDTO> userResponseDTOS) {
        Set<UUID> reversedContactIds = getReversedContactIds(userId, contacts);
        userResponseDTOS.forEach(userResponseDTO -> {
            boolean isInContact = reversedContactIds.contains(userResponseDTO.getUserProfileResponseDTO().getId());
            userResponseDTO.getUserProfileResponseDTO().getPrivacySettings().setInContactList(isInContact);
        });
        return userResponseDTOS;
    }
    private Set<UUID> getReversedContactIds(UUID userId, List<Contacts> contacts) {
        Set<UUID> contactIds = contacts.stream()
                .map(Contacts::getUserContactId)
                .collect(Collectors.toSet());
        return contactsRepository.findReversedContactIds(userId, contactIds);
    }
    public List<FeignClientUserProfileResponseDTO> getContactInformationOfExistingChats(ContactInformationOfExistingChatsRequestDTO contactInformationOfExistingChatsRequestDTO) {
        List<Contacts> contacts = this.contactsRepository.findContactsByUserIdAndUserContactIds(contactInformationOfExistingChatsRequestDTO.getUserId(), contactInformationOfExistingChatsRequestDTO.getUserContactIds());


        List<FeignClientUserProfileRequestDTO> contactList = IContactsMapper.INSTANCE.toContactRequestList(contacts);
        List<FeignClientUserProfileResponseDTO> userResponseDTOS = this.userManager.getUserList(contactList);


        Map<UUID, Contacts> contactsMap = contacts.stream()
                .collect(Collectors.toMap(Contacts::getUserContactId, contact -> contact));

        Set<UUID> reversedContactIds = getReversedContactIds(contactInformationOfExistingChatsRequestDTO.getUserId(), contacts);

        return userResponseDTOS.stream()
                .map(user -> {
                    Contacts contact = contactsMap.get(user.getUserProfileResponseDTO().getId());
                    boolean isInContact = reversedContactIds.contains(user.getUserProfileResponseDTO().getId());
                    return FeignClientUserProfileResponseDTO.builder()
                            .id(contact.getId())
                            .userContactName(contact.getUserContactName())
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
                                            .isInContactList(isInContact).build()).build())
                            .build();
                })
                .toList();

    }

    private FeignClientUserProfileResponseDTO convertInvitationToContact(Invitation invitation) {
        System.out.println("INVITATION > " + invitation);
        InvitationResponseDTO invitationResponseDTO = IInvitationMapper.INSTANCE.toInvitationResponseDTO(invitation);
        System.out.println("INVITATIONDTO > " + invitationResponseDTO);
        return FeignClientUserProfileResponseDTO.builder()
                .invitationResponseDTO(invitationResponseDTO)
                .build();
    }


    /** * Arkadaşlık isteği gönderip onaylayarak Arkadaş ekleme işlemleri >>
     * Arkadaşlık isteği bildirimi gönderme
     * Arkadaşlık onay bildirimi gönderme
     * Onaylanmış arkadaşları çekme
     * AddFriend2.js - WebSocket
     public void addToFriends(FriendRequestRequestDTOWS dto) {
     TokenResponseDTO tokenResponseDTO = this.userManager.feignClientGetUserId(dto.getToken());
     System.out.println(tokenResponseDTO);
     System.out.println(!this.friendshipsRepository.existsByUserIdAndFriendId(tokenResponseDTO.getUserId(), dto.getFriendId()));
     if(!this.friendshipsRepository.existsByUserIdAndFriendId(tokenResponseDTO.getUserId(), dto.getFriendId())) {
     System.out.println("iffffffffffffffffffff");
     Friendships friendships = friendshipsRepository.save(Friendships.builder()
     .userId(tokenResponseDTO.getUserId())
     .friendId(dto.getFriendId())
     .friendEmail(dto.getFriendEmail())
     .friendshipStatus(FriendshipStatus.SENT)
     .userEmail(tokenResponseDTO.getEmail())
     .build());
     System.out.println("Arkadas istegini gonderen Kullanici: " + friendships.getUserId().toString() + " EMAIL: " + friendships.getUserEmail());
     messagingTemplate.convertAndSendToUser(friendships.getFriendId().toString(),"/queue/friend-request-friend-response", FriendRequestNotification.builder().senderId(friendships.getUserId()).recipientId(friendships.getFriendId()).build());
     System.out.println("Arkadaslik istegini alan Kullanici: " + friendships.getFriendId().toString() + " EMAIL: " + friendships.getFriendEmail());
     messagingTemplate.convertAndSendToUser(friendships.getUserId().toString(),"/queue/friend-request-user-response", HttpResponse.builder()
     .message("Arkadaş Ekleme İsteği Gönderildi")
     .statusCode(200).build());
     } else {
     System.out.println("elseeeeeeeeeeeeeeeeeeeeeeeee");
     messagingTemplate.convertAndSendToUser(tokenResponseDTO.getUserId().toString(),"/queue/friend-request-user-response", HttpResponse.builder()
     .message("Arkadaş Ekleme İsteği Zaten Gönderilmiş")
     .statusCode(400).build());
     }
     }

     public List<FeignClientUserProfileResponseDTO> getFriendList(String authorization) {
     System.out.println("AUTHORIZATION: " + authorization);
     TokenResponseDTO tokenResponseDTO = this.userManager.feignClientGetUserId(authorization);
     System.out.println("USERIDDDDDDDDDDDD>>>>>>>>>> " + tokenResponseDTO);
     List<Friendships> friendships = this.friendshipsRepository.findByUserIdAndFriendshipStatus(tokenResponseDTO.getUserId(), FriendshipStatus.APPROVED);
     System.out.println("Friendships: " + friendships);

     List<FeignClientUserProfileRequestDTO> userProfileRequestDTOList = IFriendshipsMapper.INSTANCE.toResponseLists(friendships);
     System.out.println("userProfileRequest: " + userProfileRequestDTOList);
     List<FeignClientUserProfileResponseDTO> userResponseDTOS = this.userManager.getUserList(userProfileRequestDTOList);

     System.out.println("mapUserResponseDTOS: " + userResponseDTOS);
     return userResponseDTOS;
     }

     public List<AwaitingApprovalResponseDTO> awaitingApproval(String authorization) {
     TokenResponseDTO tokenResponseDTO = this.userManager.feignClientGetUserId(authorization);
     List<Friendships> friendshipsList = this.friendshipsRepository.findSentFriendshipsOrderByCreatedAtDesc(tokenResponseDTO.getUserId(), FriendshipStatus.SENT);
     return IFriendshipsMapper.INSTANCE.toResponseList(friendshipsList);
     }

     public void friendRequestReply(FriendRequestReplyRequestDTOWS friendRequestReplyDTOWS) {
     System.out.println("REQUESTDTO: "+ friendRequestReplyDTOWS);
     Optional<Friendships> friendships = friendshipsRepository.findOptionalByUserIdAndFriendId(friendRequestReplyDTOWS.getUserId(), friendRequestReplyDTOWS.getFriendId());
     if (friendRequestReplyDTOWS.isAccepted() && friendships.isPresent()){
     System.out.println("APPROVED IF");
     friendships.get().setFriendshipStatus(FriendshipStatus.APPROVED);
     Friendships reverseFriendships = Friendships.builder().friendshipStatus(FriendshipStatus.APPROVED).friendEmail(friendships.get().getUserEmail()).friendId(friendships.get().getUserId()).userEmail(friendships.get().getFriendEmail()).userId(friendships.get().getFriendId()).build();
     friendshipsRepository.save(reverseFriendships);
     friendshipsRepository.save(friendships.get());
     messagingTemplate.convertAndSendToUser(friendships.get().getUserId().toString(),"/queue/friend-request-reply-notification-user-response", friendships.get().getFriendEmail());
     messagingTemplate.convertAndSendToUser(friendships.get().getFriendId().toString(),"/queue/friend-request-reply-notification-friend-response", friendships.get().getUserEmail());
     } else {
     System.out.println("APPROVED ELSE");
     friendships.get().setFriendshipStatus(FriendshipStatus.DENIED);
     friendshipsRepository.save(friendships.get());
     }
     }

     public List<FriendRequestReplyNotificationsResponseDTO> friendRequestReplyNotifications(String authorization) {
     TokenResponseDTO tokenResponseDTO = this.userManager.feignClientGetUserId(authorization);
     Optional<List<Friendships>> friendshipsList = this.friendshipsRepository.friendRequestReplyNotifications(tokenResponseDTO.getUserId(), FriendshipStatus.APPROVED);
     return IFriendshipsMapper.INSTANCE.toReplyResponseList(friendshipsList.orElse(null));
     }*/

}
