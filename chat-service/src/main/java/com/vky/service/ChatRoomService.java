package com.vky.service;

import com.vky.dto.LastMessageInfo;
import com.vky.dto.request.*;
import com.vky.dto.response.*;
import com.vky.manager.IContactsManager;
import com.vky.manager.IUserManager;
import com.vky.mapper.IChatMapper;
import com.vky.rabbitmq.RabbitMQConsumer;
import com.vky.rabbitmq.RabbitMQProducer;
import com.vky.repository.IChatRoomRepository;
import com.vky.repository.entity.ChatMessage;
import com.vky.repository.entity.ChatRoom;
import com.vky.repository.entity.UserChatSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final IChatRoomRepository chatRoomRepository;
    private final ChatMessageService chatMessageService;
    private final IUserManager userManager;
    private final UserChatSettingsService userChatSettingsService;
    private final IContactsManager contactsManager;
    private final RabbitMQProducer rabbitMQProducer;
    private final RabbitMQConsumer rabbitMQConsumer;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatRoom chatRoomSave(String userId, String friendId) {
        List<String> participantIds = new ArrayList<>();
        participantIds.add(userId);
        participantIds.add(friendId);
        return this.chatRoomRepository.save(ChatRoom.builder().participantIds(new ArrayList<>(participantIds)).build());
    }

    public List<ChatRoomResponseDTO> getChatList(String userId) {
        List<ChatRoom> chatRooms = this.chatRoomRepository.findByParticipantIdsContaining(userId);
        List<ChatRoomResponseDTO> chatRoomResponseDTOs = new ArrayList<>();

        for (ChatRoom chatRoom : chatRooms) {
            List<String> filteredParticipantIds = chatRoom.getParticipantIds()
                    .stream()
                    .filter(id -> !id.equals(userId))
                    .toList();

            String friendId = filteredParticipantIds.isEmpty() ? null : filteredParticipantIds.get(0);

            List<ChatMessage> messages = chatMessageService.getChatMessages(chatRoom.getId());
            List<ChatRoomMessageResponseDTO> messageDTOs = messages.stream()
                    .map(IChatMapper.INSTANCE::chatMessageToDTO)
                    .collect(Collectors.toList());
            ChatRoomResponseDTO chatRoomDTO = ChatRoomResponseDTO.builder().messages(messageDTOs).userId(userId).friendId(friendId).id(chatRoom.getId()).build();

            String friendEmail = this.userManager.getUserEmailById(UUID.fromString(friendId));
            chatRoomDTO.setFriendEmail(friendEmail);
            chatRoomResponseDTOs.add(chatRoomDTO);
        }

        return chatRoomResponseDTOs;
    }

    /*    public ChatRoomResponseDTO getChatMessage(ChatRequestDTO chatRequestDTO) {
            ChatRoom chatRoom = this.chatRoomRepository.findByUserIdAndFriendIdOrFriendIdAndUserId(chatRequestDTO.getUserId(), chatRequestDTO.getFriendId(), chatRequestDTO.getUserId(), chatRequestDTO.getFriendId());
            ChatRoomResponseDTO chatRoomDTO;
            if (chatRoom != null) {
                List<ChatRoomMessageResponseDTO> messageDTOs = this.chatMessageService.getChatMessages(chatRoom.getId()).stream()
                        .map(IChatMapper.INSTANCE::chatMessageToDTO)
                        .collect(Collectors.toList());
                chatRoomDTO = ChatRoomResponseDTO.builder().messages(messageDTOs).userId(chatRequestDTO.getUserId()).friendId(chatRequestDTO.getFriendId()).friendEmail(chatRequestDTO.getFriendEmail()).id(chatRoom.getId()).build();
                //chatRoomDTO = IChatMapper.INSTANCE.chatRoomToDTO(chatRoom, messageDTOs);
            }
            else {
                chatRoomDTO = ChatRoomResponseDTO.builder().messages(null).userId(chatRequestDTO.getUserId()).friendEmail(chatRequestDTO.getFriendEmail()).friendId(chatRequestDTO.getFriendId()).id(null).build();


            }
            return chatRoomDTO;
        }*/
    public ChatRoomWithUserChatSettingsDTO findByParticipantIds(String userId, String friendId) {
        List<String> ids = new ArrayList<>();
        ids.add(userId);
        ids.add(friendId);
        System.out.println("IDS > " + ids.toString());
        ChatRoom chatRoom = this.chatRoomRepository.findByParticipantIdsContainsAll(ids);
        if (chatRoom != null) {
            UserChatSettings userChatSettings = userChatSettingsService.findByUserIdAndChatRoomId(userId, chatRoom.getId());
            return ChatRoomWithUserChatSettingsDTO.builder().userId(userId).participantIds(chatRoom.getParticipantIds()).friendId(friendId).userChatSettings(userChatSettings).id(chatRoom.getId()).build();
        } else {
            ChatRoom chatRoomSave = chatRoomSave(userId, friendId);
            UserChatSettings userChatSettings = userChatSettingsService.saveUserChatSettings(chatRoomSave.getId(), userId);
            userChatSettingsService.saveUserChatSettings(chatRoomSave.getId(), friendId);
            return ChatRoomWithUserChatSettingsDTO.builder().id(chatRoomSave.getId()).userId(userId).participantIds(chatRoomSave.getParticipantIds()).friendId(friendId).userChatSettings(userChatSettings).build();
        }
    }

    public CheckChatRoomExistsResponseDTO checkChatRoomExists(String userId, String friendId) {
        List<String> ids = new ArrayList<>();
        ids.add(userId);
        ids.add(friendId);
        ChatRoom chatRoom = this.chatRoomRepository.findByParticipantIdsContainsAll(ids);
        CheckChatRoomExistsResponseDTO checkChatRoomExistsResponseDTO = new CheckChatRoomExistsResponseDTO();

        if (chatRoom != null) {
            UserChatSettings userChatSettings = userChatSettingsService.findByUserIdAndChatRoomId(userId, chatRoom.getId());
            UserChatSettingsDTO userChatSettingsDTO = IChatMapper.INSTANCE.userChatSettingsToDTO(userChatSettings);
            checkChatRoomExistsResponseDTO.setId(chatRoom.getId());
            checkChatRoomExistsResponseDTO.setExists(true);
            checkChatRoomExistsResponseDTO.setUserChatSettings(userChatSettingsDTO);
        } else {
            checkChatRoomExistsResponseDTO.setExists(false);
        }
        return checkChatRoomExistsResponseDTO;

    }

//    public void sendMessage(MessageRequestDTO messageRequestDTO) {
//            this.chatMessageService.sendMessage(messageRequestDTO);
//    }


    public List<ChatRoom> getUserChatRooms(String userId) {
        return chatRoomRepository.findByParticipantIdsContaining(userId);
    }

    public List<ChatRoom> getUserChatRoomsAndDeletedFalse(List<String> chatRoomIds) {
        return chatRoomRepository.findAllByChatRoomIdsIn(chatRoomIds);
    }

    public List<ChatMessage> getChatMessages(String chatRoomId) {
        return chatMessageService.getChatMessages(chatRoomId);
    }

    public ChatMessage getChatLastMessage(String chatRoomId) {
        return chatMessageService.getChatLastMessage(chatRoomId);
    }

    public boolean isUserBlocked(String userId, String chatRoomId) {
        UserChatSettings settings = userChatSettingsService.findByUserIdAndChatRoomId(userId, chatRoomId);
        return settings != null && settings.isBlocked();
    }

    //    public void processMessage(MessageRequestDTO messageRequestDTO) {
//        boolean isSenderBlocked = isUserBlocked(messageRequestDTO.getSenderId(), messageRequestDTO.getChatRoomId());
//        boolean isRecipientBlocked = isUserBlocked(messageRequestDTO.getRecipientId(), messageRequestDTO.getChatRoomId());
//
//        boolean isSuccess = !(isSenderBlocked || isRecipientBlocked);
//        chatMessageService.sendMessage(messageRequestDTO, isSuccess);
//    }
    public void processMessage(MessageRequestDTO messageRequestDTO) {
        boolean isSenderBlocked = isUserBlocked(messageRequestDTO.getSenderId(), messageRequestDTO.getChatRoomId());
        boolean isRecipientBlocked = isUserBlocked(messageRequestDTO.getRecipientId(), messageRequestDTO.getChatRoomId());

        if (isSenderBlocked || isRecipientBlocked) {
            chatMessageService.sendErrorNotification(messageRequestDTO, isSenderBlocked);
            return;
        }

        rabbitMQProducer.sendMessage(messageRequestDTO);
    }



    public List<ChatRoomWithMessagesDTO> getUserChatRoomsAndMessages(String userId) {
        List<ChatRoom> chatRooms = getUserChatRooms(userId);

        return chatRooms.stream().map(chatRoom -> {
            List<String> filteredParticipantIds = chatRoom.getParticipantIds()
                    .stream()
                    .filter(id -> !id.equals(userId))
                    .toList();

            String friendId = filteredParticipantIds.isEmpty() ? null : filteredParticipantIds.get(0);
            List<ChatMessage> messages = getChatMessages(chatRoom.getId());
            List<ChatRoomMessageResponseDTO> messageDTOs = messages.stream()
                    .map(IChatMapper.INSTANCE::chatMessageToDTO)
                    .collect(Collectors.toList());
            UserChatSettings userChatSettings = userChatSettingsService.findByUserIdAndChatRoomId(userId, chatRoom.getId());

            String friendEmail = friendId != null ? this.userManager.getUserEmailById(UUID.fromString(friendId)) : null;

            return ChatRoomWithMessagesDTO.builder()
                    .id(chatRoom.getId())
                    .image(null)
                    .messages(messageDTOs)
                    .userChatSettings(userChatSettings)
                    .friendEmail(friendEmail)
                    .friendId(friendId)
                    .userId(userId)
                    .build();
        }).collect(Collectors.toList());
    }

    //    public List<ChatSummaryDTO> getUserChatSummariess(String userId) {
//        List<ChatRoom> chatRooms = getUserChatRooms(userId);
//        List<UUID> participantIds = extractParticipantIds(chatRooms, userId);
//        List<String> chatRoomIds = extractChatRoomIds(chatRooms);
//
//        Map<String, LastMessageInfo> lastMessageMap = chatMessageService.getLastMessagesForChatRooms(chatRoomIds);
//        Map<String, UserChatSettings> userChatSettingsMap = userChatSettingsService.findUserChatSettingsByUserIdAndChatRoomIds(userId, chatRoomIds);
//
//        List<FeignClientUserProfileResponseDTO> profiles = getParticipantProfiles(userId,participantIds);
//
//        return chatRooms.stream()
//                .map(chatRoom -> buildChatSummaryDTO(chatRoom, lastMessageMap, userChatSettingsMap, profiles, userId))
//                .filter(Objects::nonNull)
//                .toList();
//    }
    public List<ChatSummaryDTO> getUserChatSummariess(String userId) {
        Map<String, UserChatSettings> userChatSettingsMap = userChatSettingsService.findUserChatSettingsByUserId(userId);
        List<String> chatRoomIds = extractChatRoomIdsUserChatSettingsMap(userChatSettingsMap);
        userChatSettingsMap.values().forEach(x -> System.out.println("X > " + x.getChatRoomId()));
        List<ChatRoom> chatRooms = getUserChatRoomsAndDeletedFalse(chatRoomIds);
        chatRooms.forEach(x -> System.out.println("------------------------------------------- > " + x.getId()));
        List<UUID> participantIds = extractParticipantIds(chatRooms, userId);


        Map<String, LastMessageInfo> lastMessageMap = chatMessageService.getLastMessagesForChatRooms(chatRoomIds);


        Map<UUID, FeignClientUserProfileResponseDTO> profileResponseDTOMap = getParticipantProfiles(userId, participantIds).stream()
                .collect(Collectors.toMap(profile -> profile.getUserProfileResponseDTO().getId(), Function.identity()));
        System.out.println("############################################ > ");
        profileResponseDTOMap.values().forEach(x -> System.out.println("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY > " + x.getUserProfileResponseDTO().getEmail()));
        List<ChatSummaryDTO> sortedChatSummaries = chatRooms.stream()
                .map(chatRoom -> buildChatSummary(chatRoom, lastMessageMap.get(chatRoom.getId()), userChatSettingsMap.get(chatRoom.getId()), userId, profileResponseDTOMap))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(chatSummary -> extractNumericPart(chatSummary.getUserProfileResponseDTO().getEmail())))
                .collect(Collectors.toList());

        // Sıralama sonrası email değerlerini yazdırarak doğrulama yapalım
        sortedChatSummaries.forEach(chatSummary ->
                System.out.println("Email: " + chatSummary.getUserProfileResponseDTO().getEmail())
        );

        return sortedChatSummaries;
    }

    private int extractNumericPart(String email) {
        // `user` ve `@gmail.com` gibi sabit kısımları ayırarak sadece sayıyı alıyoruz
        String numericPart = email.replaceAll("[^0-9]", ""); // Sadece sayıları bırak
        return Integer.parseInt(numericPart); // Sayısal değere çevir
    }

    public ChatSummaryDTO getUserChatSummary(String userId, String userContactId, String chatRoomId) {
        // ToDo  String userContactId && UserChatSettings deleted and deletedTime
        ChatRoom chatRoom = chatRoomRepository.findChatRoomById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found for id: " + chatRoomId));

        UserChatSettings userChatSettings = userChatSettingsService.findUserChatSettingsByUserIdAndChatRoomId(userId, chatRoom.getId());
        ChatMessage chatMessage = chatMessageService.getLastMessageForChatRooms(chatRoomId);

        FeignClientUserProfileResponseDTO profileResponse = contactsManager.getContactInformationOfExistingChat(ContactInformationOfExistingChatRequestDTO.builder()
                .userId(UUID.fromString(userId))
                .userContactId(UUID.fromString(userContactId))
                .build());
        return ChatSummaryDTO.builder()
                .chatDTO(ChatDTO.builder()
                        .id(chatRoom.getId())
                        .messageId(chatMessage.getId())
                        .participantIds(chatRoom.getParticipantIds())
                        .senderId(chatMessage.getSenderId())
                        .recipientId(chatMessage.getRecipientId())
                        .lastMessageTime(chatMessage.getFullDateTime())
                        .lastMessage(chatMessage.getMessageContent())
                        .isSeen(chatMessage.isSeen())
                        .build())
                .contactsDTO(profileResponse.getContactsDTO())
                .userChatSettings(mapUserChatSettingsDTO(userChatSettings))
                .userProfileResponseDTO(profileResponse.getUserProfileResponseDTO())
                .build();
    }

    private ChatSummaryDTO buildChatSummary(ChatRoom chatRoom, LastMessageInfo lastMessageInfo, UserChatSettings userChatSettings, String userId, Map<UUID, FeignClientUserProfileResponseDTO> profileMap) {
        if (lastMessageInfo == null) {
            return null;
        }
        UUID senderOrRecipientId = UUID.fromString(
                lastMessageInfo.getSenderId().equals(userId) ? lastMessageInfo.getRecipientId() : lastMessageInfo.getSenderId()
        );
        FeignClientUserProfileResponseDTO profile = profileMap.get(senderOrRecipientId);
        if (profile == null) {
            return null;
        }
        return ChatSummaryDTO.builder()
                .contactsDTO(profile.getContactsDTO())
                .chatDTO(ChatDTO.builder()
                        .id(chatRoom.getId())
                        .messageId(lastMessageInfo.getId())
                        .participantIds(chatRoom.getParticipantIds())
                        .lastMessage(lastMessageInfo.getLastMessage())
                        .lastMessageTime(lastMessageInfo.getLastMessageTime())
                        .recipientId(lastMessageInfo.getRecipientId())
                        .senderId(lastMessageInfo.getSenderId())
                        .isSeen(lastMessageInfo.isSeen())
                        .build())
                .userChatSettings(mapUserChatSettingsDTO(userChatSettings))
                .userProfileResponseDTO(profile.getUserProfileResponseDTO())
                .build();
    }

    private List<UUID> extractParticipantIds(List<ChatRoom> chatRooms, String userId) {
        return chatRooms.stream()
                .map(ChatRoom::getParticipantIds)
                .flatMap(Collection::stream)
                .filter(participantId -> !participantId.equals(userId))
                .map(UUID::fromString)
                .toList();
    }

    private List<String> extractChatRoomIds(List<ChatRoom> chatRooms) {
        return chatRooms.stream()
                .map(ChatRoom::getId)
                .toList();
    }

    private List<String> extractChatRoomIdsUserChatSettingsMap(Map<String, UserChatSettings> userChatSettingsMap) {
        return userChatSettingsMap.values().stream()
                .map(UserChatSettings::getChatRoomId)
                .toList();
    }

    private List<FeignClientUserProfileResponseDTO> getParticipantProfiles(String userId, List<UUID> participantIds) {
        ContactInformationOfExistingChatsRequestDTO request = ContactInformationOfExistingChatsRequestDTO.builder()
                .userId(UUID.fromString(userId))
                .userContactIds(participantIds)
                .build();
        return contactsManager.getContactInformationOfExistingChats(request);
    }


    private UserChatSettingsDTO mapUserChatSettingsDTO(UserChatSettings settings) {
        if (settings == null) return null;
        return IChatMapper.INSTANCE.userChatSettingsToDTO(settings);
    }

    /**
     * CommandLineRunner açılırsa açılacak
     * public List<ChatSummaryDTO> getUserChatSummariess(String userId) {
     * List<ChatRoom> chatRooms = getUserChatRooms(userId);
     * <p>
     * return chatRooms.stream().map(chatRoom -> {
     * List<String> filteredParticipantIds = chatRoom.getParticipantIds()
     * .stream()
     * .filter(id -> !id.equals(userId))
     * .toList();
     * <p>
     * String friendId = filteredParticipantIds.isEmpty() ? null : filteredParticipantIds.get(0);
     * ChatMessage chatMessage = getChatLastMessage(chatRoom.getId());
     * <p>
     * // Null kontrolü
     * String lastMessageContent = chatMessage != null ? chatMessage.getMessageContent() : "No message available";
     * Instant lastMessageTime = chatMessage != null ? chatMessage.getFullDateTime() : Instant.now();
     * <p>
     * UserChatSettings userChatSettings = userChatSettingsService.findByUserIdAndChatRoomId(userId, chatRoom.getId());
     * UserChatSettingsDTO userChatSettingsDTO = IChatMapper.INSTANCE.userChatSettingsToDTO(userChatSettings);
     * String friendEmail = friendId != null ? this.userManager.getUserEmailById(UUID.fromString(friendId)) : null;
     * <p>
     * return ChatSummaryDTO.builder()
     * .id(chatRoom.getId())
     * .image(null)
     * .lastMessage(lastMessageContent)
     * .lastMessageTime(lastMessageTime)
     * .userChatSettings(userChatSettingsDTO)
     * .friendEmail(friendEmail)
     * .friendId(friendId)
     * .userId(userId)
     * .build();
     * }).collect(Collectors.toList());
     * }
     */


    public MessagesDTO getLast30Messages(String chatRoomId, int limit, String userId) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "fullDateTime"));
        return chatMessageService.getLast30Messages(chatRoomId, pageable);
    }

    public MessagesDTO getOlderMessages(String chatRoomId, Instant before, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "fullDateTime"));
        return chatMessageService.getOlderMessages(chatRoomId, before, pageable);
    }

    public boolean chatBlock(ChatSummaryDTO chatSummaryDTO) {
        try {
            UserChatSettings[] userChatSettingsArr = new UserChatSettings[2];
            UserChatSettings userChatSettings = this.userChatSettingsService.findByUserIdAndChatRoomId(chatSummaryDTO.getContactsDTO().getUserId().toString(), chatSummaryDTO.getChatDTO().getId());
            userChatSettings.setBlocked(true);
            userChatSettings.setBlockedTime(Instant.now());
            UserChatSettings contactsUserChatSettings = this.userChatSettingsService.findByUserIdAndChatRoomId(chatSummaryDTO.getContactsDTO().getUserContactId().toString(), chatSummaryDTO.getChatDTO().getId());
            contactsUserChatSettings.setBlockedMe(true);
            contactsUserChatSettings.setBlockedTime(Instant.now());
            userChatSettingsArr[0] = userChatSettings;
            userChatSettingsArr[1] = contactsUserChatSettings;
            userChatSettingsService.updateUserChatSettingsSaveAll(userChatSettingsArr);
            messagingTemplate.convertAndSendToUser(chatSummaryDTO.getContactsDTO().getUserContactId().toString(), "/queue/block", contactsUserChatSettings);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean chatUnblock(ChatSummaryDTO chatSummaryDTO) {
        try {
            UserChatSettings[] userChatSettingsArr = new UserChatSettings[2];
            UserChatSettings userChatSettings = this.userChatSettingsService.findByUserIdAndChatRoomId(chatSummaryDTO.getContactsDTO().getUserId().toString(), chatSummaryDTO.getChatDTO().getId());
            userChatSettings.setBlocked(false);
            userChatSettings.setUnblockedTime(Instant.now());
            UserChatSettings contactsUserChatSettings = this.userChatSettingsService.findByUserIdAndChatRoomId(chatSummaryDTO.getContactsDTO().getUserContactId().toString(), chatSummaryDTO.getChatDTO().getId());
            contactsUserChatSettings.setBlockedMe(false);
            contactsUserChatSettings.setUnblockedTime(Instant.now());
            userChatSettingsArr[0] = userChatSettings;
            userChatSettingsArr[1] = contactsUserChatSettings;
            userChatSettingsService.updateUserChatSettingsSaveAll(userChatSettingsArr);
            messagingTemplate.convertAndSendToUser(chatSummaryDTO.getContactsDTO().getUserContactId().toString(), "/queue/unblock", contactsUserChatSettings);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean chatOtherSettings(ChatSummaryDTO chatSummaryDTO) {
        try {
            UserChatSettings userSettings = this.userChatSettingsService.findByUserIdAndChatRoomId(chatSummaryDTO.getContactsDTO().getUserId().toString(), chatSummaryDTO.getChatDTO().getId());
            userSettings.setArchived(chatSummaryDTO.getUserChatSettings().isArchived());
            userSettings.setPinned(chatSummaryDTO.getUserChatSettings().isPinned());
            userChatSettingsService.updateUserChatSettings(userSettings);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void deleteChat(UserChatSettingsDTO userChatSettingsDTO) {
        UserChatSettings userSettings = this.userChatSettingsService.findByUserIdAndChatRoomId(userChatSettingsDTO.getUserId(), userChatSettingsDTO.getChatRoomId());
        if (userSettings == null) {
            throw new IllegalArgumentException("Chat settings not found");
        }
        userSettings.setDeleted(true);
        // ToDo Eğer bu chat ten tekrar mesaj gelir veya bu chat e mesaj gönderilirse. deletedTime dan sonraki mesajlar cekilecek ve tekrar deleted false olarak güncellenmeli
        userSettings.setDeletedTime(Instant.now());
        userChatSettingsService.updateUserChatSettings(userSettings);
    }

    public void readMessage(UnreadMessageCountDTO unreadMessageCountDTO) {
        rabbitMQProducer.readMessage(unreadMessageCountDTO);
    }

//    public void createChatRoomAndFristMessage(CreateChatRoomDTO createChatRoomDTO) {
//        ChatRoom chatRoom = chatRoomSave(createChatRoomDTO.getUserId(), createChatRoomDTO.getFriendId());
//        boolean isSenderBlocked = isUserBlocked(createChatRoomDTO.getUserId(), chatRoom.getId());
//        boolean isRecipientBlocked = isUserBlocked(createChatRoomDTO.getFriendId(), chatRoom.getId());
//
//        boolean isSuccess = !(isSenderBlocked || isRecipientBlocked);
//        String destination = isSuccess ? "queue/received-message" : "/queue/error";
//        ChatMessage chatMessage = this.chatMessageService.sendFirstMessage(createChatRoomDTO, chatRoom.getId());
//        UserChatSettings userChatSettings = userChatSettingsService.findByUserIdAndChatRoomId(createChatRoomDTO.getUserId(), chatRoom.getId());
//        UserChatSettings friendChatSettings = userChatSettingsService.findByUserIdAndChatRoomId(createChatRoomDTO.getFriendId(), chatRoom.getId());
//        if(userChatSettings == null) {
//            userChatSettings = this.userChatSettingsService.saveUserChatSettings(chatRoom.getId(),createChatRoomDTO.getUserId());
//        }
//        if(friendChatSettings == null) {
//            friendChatSettings = this.userChatSettingsService.saveUserChatSettings(chatRoom.getId(),createChatRoomDTO.getFriendId());
//
//        }
//
//        ChatSummaryDTO userChatSumamryDTO = ChatSummaryDTO.builder()
//                .id(chatRoom.getId())
//                .image(null)
//                .lastMessage(chatMessage.getMessageContent())
//                .lastMessageTime(chatMessage.getFullDateTime())
//                .messages(null)
//                .userChatSettings(userChatSettings)
//                .friendEmail(createChatRoomDTO.getFriendEmail())
//                .friendId(createChatRoomDTO.getFriendId())
//                .userId(createChatRoomDTO.getUserId())
//                .build();
//        messagingTemplate.convertAndSendToUser(
//                isSuccess ? createChatRoomDTO.getFriendId() : createChatRoomDTO.getUserId(),
//                destination,
//                userChatSumamryDTO);
//        String userEmail = createChatRoomDTO.getUserId() != null ? this.userManager.getUserEmailById(UUID.fromString(createChatRoomDTO.getUserId())) : null;
//
//        ChatSummaryDTO friendChatSummaryDTO = ChatSummaryDTO.builder()
//                .id(chatRoom.getId())
//                .image(null)
//                .lastMessage(chatMessage.getMessageContent())
//                .lastMessageTime(chatMessage.getFullDateTime())
//                .messages(null)
//                .userChatSettings(friendChatSettings)
//                .friendEmail(userEmail)
//                .friendId(createChatRoomDTO.getUserId())
//                .userId(createChatRoomDTO.getFriendId())
//                .build();
//        messagingTemplate.convertAndSendToUser(
//                createChatRoomDTO.getUserId(),
//                destination,
//                friendChatSummaryDTO);
//    }


}
