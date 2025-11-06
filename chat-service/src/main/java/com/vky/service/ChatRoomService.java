package com.vky.service;

import com.vky.dto.LastMessageInfo;
import com.vky.dto.request.ContactInformationOfExistingChatRequestDTO;
import com.vky.dto.request.ContactInformationOfExistingChatsRequestDTO;
import com.vky.dto.request.MessageRequestDTO;
import com.vky.dto.request.UnreadMessageCountDTO;
import com.vky.dto.response.*;
import com.vky.expcetion.ErrorType;
import com.vky.manager.IContactsManager;
import com.vky.mapper.IChatMapper;
import com.vky.rabbitmq.RabbitMQProducer;
import com.vky.repository.IChatRoomRepository;
import com.vky.repository.IUserChatSettingsRepository;
import com.vky.repository.entity.ChatMessage;
import com.vky.repository.entity.ChatRoom;
import com.vky.repository.entity.UserChatSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final IChatRoomRepository chatRoomRepository;
    private final ChatMessageService chatMessageService;
    private final UserChatSettingsService userChatSettingsService;
    private final IContactsManager contactsManager;
    private final RabbitMQProducer rabbitMQProducer;
    private final SimpMessagingTemplate messagingTemplate;
    private final UnreadMessageCountService unreadMessageCountService;
    private final IUserChatSettingsRepository iUserChatSettingsRepository;

    public ChatRoom chatRoomSave(String userId, String friendId) {
        List<String> participantIds = new ArrayList<>();
        participantIds.add(userId);
        participantIds.add(friendId);
        return this.chatRoomRepository.save(ChatRoom.builder().participantIds(new ArrayList<>(participantIds)).build());
    }

    public ChatRoomWithUserChatSettingsDTO findByParticipantIds(String userId, String friendId) {
        List<String> ids = new ArrayList<>();
        ids.add(userId);
        ids.add(friendId);
        ChatRoom chatRoom = this.chatRoomRepository.findByParticipantIdsContainsAll(ids);
        if (chatRoom != null) {
            UserChatSettings userChatSettings = userChatSettingsService.findByUserIdAndChatRoomId(userId, chatRoom.getId());
            userChatSettings.setDeleted(false);
            userChatSettings.setDeletedTime(Instant.now());
            iUserChatSettingsRepository.save(userChatSettings);
            return ChatRoomWithUserChatSettingsDTO.builder().userId(userId).participantIds(chatRoom.getParticipantIds()).friendId(friendId).userChatSettingsDTO(IChatMapper.INSTANCE.userChatSettingsToDTO(userChatSettings)).id(chatRoom.getId()).build();
        } else {
            ChatRoom chatRoomSave = chatRoomSave(userId, friendId);
            UserChatSettings userChatSettings = userChatSettingsService.saveUserChatSettings(chatRoomSave.getId(), userId,friendId);
            userChatSettingsService.saveUserChatSettings(chatRoomSave.getId(), friendId,userId);
            return ChatRoomWithUserChatSettingsDTO.builder().id(chatRoomSave.getId()).userId(userId).participantIds(chatRoomSave.getParticipantIds()).friendId(friendId).userChatSettingsDTO(IChatMapper.INSTANCE.userChatSettingsToDTO(userChatSettings)).build();
        }
    }

    public List<ChatRoom> getUserChatRoomsAndDeletedFalse(List<String> chatRoomIds) {
        return chatRoomRepository.findAllByChatRoomIdsIn(chatRoomIds);
    }



    public boolean isUserBlocked(String userId, String chatRoomId) {
        UserChatSettings settings = userChatSettingsService.findByUserIdAndChatRoomId(userId, chatRoomId);
        return settings != null && settings.isBlocked();
    }
    @Async("taskExecutor")
    public void processMessage(MessageRequestDTO messageRequestDTO) {
        boolean isSenderBlocked = isUserBlocked(messageRequestDTO.getSenderId(), messageRequestDTO.getChatRoomId());
        boolean isRecipientBlocked = isUserBlocked(messageRequestDTO.getRecipientId(), messageRequestDTO.getChatRoomId());

        if (isSenderBlocked || isRecipientBlocked) {
            ErrorType errorType = isSenderBlocked ? ErrorType.SENDER_BLOCKED : ErrorType.RECIPIENT_BLOCKED;
            chatMessageService.sendErrorNotification(messageRequestDTO, errorType);
            return;
        }
        rabbitMQProducer.sendMessage(messageRequestDTO);
    }

    @Async("taskExecutor")
    public CompletableFuture<List<ChatSummaryDTO>>  getUserChatSummaries(String userId) {
        try {
            Map<String, UserChatSettings> userChatSettings = userChatSettingsService.findUserChatSettingsByUserId(userId);

            List<String> chatRoomIds = extractChatRoomIdsUserChatSettingsMap(userChatSettings);

            List<ChatRoom> chatRooms = getUserChatRoomsAndDeletedFalse(chatRoomIds);

            Map<String, LastMessageInfo> lastMessages = chatMessageService.getLastMessagesForChatRooms(chatRoomIds);

            List<UUID> participantIds = extractParticipantIds(chatRooms, userId);
            ContactInformationOfExistingChatsRequestDTO dto = new ContactInformationOfExistingChatsRequestDTO();
            dto.setUserId(UUID.fromString(userId));
            dto.setUserContactIds(participantIds);
            List<ContactResponseDTO> profilesList = contactsManager.getContactInformationOfExistingChats(dto);
            Map<UUID, ContactResponseDTO> profiles = profilesList.stream()
                    .collect(Collectors.toMap(
                            profile -> profile.getUserProfileResponseDTO().getId(),
                            Function.identity()
                    ));

            List<ChatSummaryDTO> chatSummaries = chatRooms.stream()
                    .map(chatRoom -> buildChatSummary(
                            chatRoom,
                            lastMessages.get(chatRoom.getId()),
                            userChatSettings.get(chatRoom.getId()),
                            userId,
                            profiles
                    ))
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(
                            (ChatSummaryDTO c) -> c.getChatDTO().getMessages().get(0).getFullDateTime()
                    ).reversed())
                    .collect(Collectors.toList());
            return CompletableFuture.completedFuture(chatSummaries);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching chat summaries", e);
        }
    }

    public CompletableFuture<ChatSummaryDTO>  getUserChatSummary(String userId, String userContactId, String chatRoomId) {

        // ToDo  String userContactId && UserChatSettings deleted and deletedTime
        ChatRoom chatRoom = chatRoomRepository.findChatRoomById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found for id: " + chatRoomId));

        UserChatSettings userChatSettings = userChatSettingsService.findUserChatSettingsByUserIdAndChatRoomId(userId, chatRoom.getId());
        ChatMessage chatMessage = chatMessageService.getLastMessageForChatRooms(chatRoomId);

        ContactResponseDTO profileResponse = contactsManager.getContactInformationOfExistingChat(ContactInformationOfExistingChatRequestDTO.builder()
                .userId(UUID.fromString(userId))
                .userContactId(UUID.fromString(userContactId))
                .build());
        ChatSummaryDTO chatSummaryDTO = ChatSummaryDTO.builder()
                .chatDTO(ChatDTO.builder()
                        .id(chatRoom.getId())
                        .participantIds(chatRoom.getParticipantIds())
                        .messages(List.of(
                                MessageDTO.builder()
                                        .id(chatMessage.getId())
                                        .chatRoomId(chatRoom.getId())
                                        .senderId(chatMessage.getSenderId())
                                        .recipientId(chatMessage.getRecipientId())
                                        .encryptedKeyForRecipient(Base64.getEncoder().encodeToString(chatMessage.getEncryptedKeyForRecipient()))
                                        .encryptedKeyForSender(Base64.getEncoder().encodeToString(chatMessage.getEncryptedKeyForSender()))
                                        .encryptedMessage(Base64.getEncoder().encodeToString(chatMessage.getEncryptedMessageContent()))
                                        .iv(Base64.getEncoder().encodeToString(chatMessage.getIv()))
                                        .isSeen(chatMessage.isSeen())
                                        .fullDateTime(chatMessage.getFullDateTime())
                                        .build()
                        ))
                        .isLastPage(true)
                        .build()
                )
                .contactsDTO(profileResponse.getContactsDTO())
                .userChatSettingsDTO(mapUserChatSettingsDTO(userChatSettings))
                .userProfileResponseDTO(profileResponse.getUserProfileResponseDTO())
                .build();
        return CompletableFuture.completedFuture(chatSummaryDTO);
    }

    private ChatSummaryDTO buildChatSummary(ChatRoom chatRoom, LastMessageInfo lastMessageInfo, UserChatSettings userChatSettings, String userId, Map<UUID, ContactResponseDTO> profileMap) {
        if (lastMessageInfo == null) {
            return null;
        }
        UUID senderOrRecipientId = UUID.fromString(
                lastMessageInfo.getSenderId().equals(userId) ? lastMessageInfo.getRecipientId() : lastMessageInfo.getSenderId()
        );
        ContactResponseDTO profile = profileMap.get(senderOrRecipientId);
        if (profile == null) {
            return null;
        }
        return ChatSummaryDTO.builder()
                .contactsDTO(profile.getContactsDTO())
                .chatDTO(ChatDTO.builder()
                        .id(chatRoom.getId())
                        .participantIds(chatRoom.getParticipantIds())
                        .messages(List.of(
                                MessageDTO.builder()
                                        .id(lastMessageInfo.getId())
                                        .chatRoomId(chatRoom.getId())
                                        .senderId(lastMessageInfo.getSenderId())
                                        .recipientId(lastMessageInfo.getRecipientId())
                                        .encryptedKeyForRecipient(lastMessageInfo.getEncryptedKeyForRecipient())
                                        .encryptedKeyForSender(lastMessageInfo.getEncryptedKeyForSender())
                                        .encryptedMessage(lastMessageInfo.getEncryptedMessage())
                                        .iv(lastMessageInfo.getIv())
                                        .isSeen(lastMessageInfo.isSeen())
                                        .fullDateTime(lastMessageInfo.getLastMessageTime())
                                        .build()
                        ))
                        .isLastPage(true)
                        .build()
                )
                .userChatSettingsDTO(mapUserChatSettingsDTO(userChatSettings))
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


    private List<String> extractChatRoomIdsUserChatSettingsMap(Map<String, UserChatSettings> userChatSettingsMap) {
        return userChatSettingsMap.values().stream()
                .map(UserChatSettings::getChatRoomId)
                .toList();
    }

    private UserChatSettingsDTO mapUserChatSettingsDTO(UserChatSettings settings) {
        if (settings == null) return null;
        return IChatMapper.INSTANCE.userChatSettingsToDTO(settings);
    }

    public ChatDTO getLast30Messages(String chatRoomId, int limit, String userId) {
        UserChatSettings userChatSettings = userChatSettingsService.findByUserIdAndChatRoomId(userId, chatRoomId);
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "fullDateTime"));
        return chatMessageService.getLast30Messages(chatRoomId, pageable, userChatSettings.getDeletedTime());
    }

    public ChatDTO getOlderMessages(String chatRoomId, Instant before, int limit, String userId) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "fullDateTime"));
        return chatMessageService.getOlderMessages(chatRoomId, before, pageable);
    }

    public void chatBlock(ChatSummaryDTO chatSummaryDTO, String userId) {
        UserChatSettings[] userChatSettingsArr = new UserChatSettings[2];

        UserChatSettings userChatSettings = userChatSettingsService
                .findByUserIdAndChatRoomId(userId, chatSummaryDTO.getChatDTO().getId());

        userChatSettings.setBlocked(true);
        userChatSettings.setBlockedTime(Instant.now());

        UserChatSettings contactsUserChatSettings = userChatSettingsService
                .findByUserIdAndChatRoomId(chatSummaryDTO.getContactsDTO().getUserContactId().toString(), chatSummaryDTO.getChatDTO().getId());

        contactsUserChatSettings.setBlockedMe(true);
        contactsUserChatSettings.setBlockedTime(Instant.now());

        userChatSettingsArr[0] = userChatSettings;
        userChatSettingsArr[1] = contactsUserChatSettings;

        userChatSettingsService.updateUserChatSettingsSaveAll(userChatSettingsArr);

        messagingTemplate.convertAndSendToUser(
                chatSummaryDTO.getContactsDTO().getUserContactId().toString(),
                "/queue/block",
                contactsUserChatSettings
        );
    }

    public void chatUnblock(ChatSummaryDTO chatSummaryDTO, String userId) {
            UserChatSettings[] userChatSettingsArr = new UserChatSettings[2];
            UserChatSettings userChatSettings = this.userChatSettingsService.findByUserIdAndChatRoomId(userId, chatSummaryDTO.getChatDTO().getId());
            userChatSettings.setBlocked(false);
            userChatSettings.setUnblockedTime(Instant.now());
            UserChatSettings contactsUserChatSettings = this.userChatSettingsService.findByUserIdAndChatRoomId(chatSummaryDTO.getContactsDTO().getUserContactId().toString(), chatSummaryDTO.getChatDTO().getId());
            contactsUserChatSettings.setBlockedMe(false);
            contactsUserChatSettings.setUnblockedTime(Instant.now());
            userChatSettingsArr[0] = userChatSettings;
            userChatSettingsArr[1] = contactsUserChatSettings;
            userChatSettingsService.updateUserChatSettingsSaveAll(userChatSettingsArr);
            messagingTemplate.convertAndSendToUser(chatSummaryDTO.getContactsDTO().getUserContactId().toString(), "/queue/unblock", contactsUserChatSettings);
    }

    public void deleteChat(UserChatSettingsDTO userChatSettingsDTO, String userId) {
        UserChatSettings userSettings = this.userChatSettingsService.findByUserIdAndChatRoomId(userId, userChatSettingsDTO.getChatRoomId());
        userSettings.setDeleted(true);
        userSettings.setUnreadMessageCount(0);
        unreadMessageCountService.generateUnreadKey(userSettings.getChatRoomId(),userId);
        // ToDo Eğer bu chat ten tekrar mesaj gelir veya bu chat e mesaj gönderilirse. deletedTime dan sonraki mesajlar cekilecek ve tekrar deleted false olarak güncellenmeli
        userSettings.setDeletedTime(Instant.now());
        userChatSettingsService.updateUserChatSettings(userSettings);
        unreadMessageCountService.deleteUnreadMessageCount(userSettings.getChatRoomId(),userId);
    }

    public void readMessage(UnreadMessageCountDTO unreadMessageCountDTO) {
        rabbitMQProducer.readMessage(unreadMessageCountDTO);
    }

}
