package com.vky.service;

import com.vky.dto.LastMessageInfo;
import com.vky.dto.request.*;
import com.vky.dto.response.*;
import com.vky.expcetion.ErrorMessage;
import com.vky.expcetion.ErrorType;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

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
    private final IUserManager userManager;
    private final UserChatSettingsService userChatSettingsService;
    private final IContactsManager contactsManager;
    private final RabbitMQProducer rabbitMQProducer;
    private final RabbitMQConsumer rabbitMQConsumer;
    private final SimpMessagingTemplate messagingTemplate;
    private final WebClient webClient;

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

    public List<ChatRoom> getUserChatRooms(String userId) {
        return chatRoomRepository.findByParticipantIdsContaining(userId);
    }

    public List<ChatRoom> getUserChatRoomsAndDeletedFalse(List<String> chatRoomIds) {
        return chatRoomRepository.findAllByChatRoomIdsIn(chatRoomIds);
    }

    public List<ChatMessage> getChatMessages(String chatRoomId) {
        return chatMessageService.getChatMessages(chatRoomId);
    }

    public boolean isUserBlocked(String userId, String chatRoomId) {
        UserChatSettings settings = userChatSettingsService.findByUserIdAndChatRoomId(userId, chatRoomId);
        return settings != null && settings.isBlocked();
    }
    @Async
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

    //    public List<ChatSummaryDTO> getUserChatSummariess(String userId) {
//        Map<String, UserChatSettings> userChatSettingsMap = userChatSettingsService.findUserChatSettingsByUserId(userId);
//        List<String> chatRoomIds = extractChatRoomIdsUserChatSettingsMap(userChatSettingsMap);
//        userChatSettingsMap.values().forEach(x -> System.out.println("X > " + x.getChatRoomId()));
//        List<ChatRoom> chatRooms = getUserChatRoomsAndDeletedFalse(chatRoomIds);
//        chatRooms.forEach(x -> System.out.println("------------------------------------------- > " + x.getId()));
//        List<UUID> participantIds = extractParticipantIds(chatRooms, userId);
//
//
//        Map<String, LastMessageInfo> lastMessageMap = chatMessageService.getLastMessagesForChatRooms(chatRoomIds);
//
//
//        Map<UUID, FeignClientUserProfileResponseDTO> profileResponseDTOMap = getParticipantProfiles(userId, participantIds).stream()
//                .collect(Collectors.toMap(profile -> profile.getUserProfileResponseDTO().getId(), Function.identity()));
//        System.out.println("############################################ > ");
//        profileResponseDTOMap.values().forEach(x -> System.out.println("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY > " + x.getUserProfileResponseDTO().getEmail()));
//        List<ChatSummaryDTO> sortedChatSummaries = chatRooms.stream()
//                .map(chatRoom -> buildChatSummary(chatRoom, lastMessageMap.get(chatRoom.getId()), userChatSettingsMap.get(chatRoom.getId()), userId, profileResponseDTOMap))
//                .filter(Objects::nonNull)
//                .sorted(Comparator.comparingInt(chatSummary -> extractNumericPart(chatSummary.getUserProfileResponseDTO().getEmail())))
//                .collect(Collectors.toList());
//
//        // Sıralama sonrası email değerlerini yazdırarak doğrulama yapalım
//        sortedChatSummaries.forEach(chatSummary ->
//                System.out.println("Email: " + chatSummary.getUserProfileResponseDTO().getEmail())
//        );
//
//        return sortedChatSummaries;
//    }

    @Async("taskExecutor")
    public CompletableFuture<List<FeignClientUserProfileResponseDTO>> getParticipantProfilesAsync(String userId, List<UUID> participantIds) {
        ContactInformationOfExistingChatsRequestDTO request = ContactInformationOfExistingChatsRequestDTO.builder()
                .userId(UUID.fromString(userId))
                .userContactIds(participantIds)
                .build();
        System.out.println("WEB CLIENT > " + webClient.toString());
        return webClient.post()
                .uri("/get-contact-information-of-existing-chats")
                .body(Mono.just(request), ContactInformationOfExistingChatsRequestDTO.class)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<FeignClientUserProfileResponseDTO>>() {})
                .onErrorResume(WebClientResponseException.class, ex -> {
                    // Log error and return empty list in case of error
                    System.err.println("Error fetching participant profiles: " + ex.getMessage());
                    return Mono.just(Collections.emptyList());
                })
                .toFuture();
    }

    @Async("taskExecutor")
    public CompletableFuture<List<ChatSummaryDTO>> getUserChatSummariesAsync(String userId) {
        CompletableFuture<Map<String, UserChatSettings>> userChatSettingsFuture =
                CompletableFuture.supplyAsync(() -> userChatSettingsService.findUserChatSettingsByUserId(userId));

        CompletableFuture<List<String>> chatRoomIdsFuture = userChatSettingsFuture.thenApply(this::extractChatRoomIdsUserChatSettingsMap);

        CompletableFuture<List<ChatRoom>> chatRoomsFuture = chatRoomIdsFuture.thenApply(this::getUserChatRoomsAndDeletedFalse);

        CompletableFuture<Map<String, LastMessageInfo>> lastMessagesFuture = chatRoomIdsFuture.thenApply(chatMessageService::getLastMessagesForChatRooms);

        CompletableFuture<Map<UUID, FeignClientUserProfileResponseDTO>> profilesFuture = chatRoomsFuture.thenComposeAsync(chatRooms -> {
            List<UUID> participantIds = extractParticipantIds(chatRooms, userId);
            return getParticipantProfilesAsync(userId, participantIds).thenApply(profiles ->
                    profiles.stream().collect(Collectors.toMap(
                            profile -> profile.getUserProfileResponseDTO().getId(),
                            Function.identity()
                    ))
            );
        });

        return CompletableFuture.allOf(userChatSettingsFuture, lastMessagesFuture, profilesFuture)
                .thenCompose(v -> {
                    try {
                        Map<String, UserChatSettings> userChatSettings = userChatSettingsFuture.get();
                        Map<String, LastMessageInfo> lastMessages = lastMessagesFuture.get();
                        Map<UUID, FeignClientUserProfileResponseDTO> profiles = profilesFuture.get();
                        List<ChatRoom> chatRooms = chatRoomsFuture.get();

                        List<ChatSummaryDTO> chatSummaries = chatRooms.stream()
                                .map(chatRoom -> buildChatSummary(
                                        chatRoom,
                                        lastMessages.get(chatRoom.getId()),
                                        userChatSettings.get(chatRoom.getId()),
                                        userId,
                                        profiles
                                ))
                                .filter(Objects::nonNull)
                                .sorted(Comparator.comparingInt(chatSummary ->
                                        extractNumericPart(chatSummary.getUserProfileResponseDTO().getEmail())))
                                .collect(Collectors.toList());

                        return CompletableFuture.completedFuture(chatSummaries);
                    } catch (Exception e) {
                        CompletableFuture<List<ChatSummaryDTO>> failedFuture = new CompletableFuture<>();
                        failedFuture.completeExceptionally(new RuntimeException("Error fetching chat summaries", e));
                        return failedFuture;
                    }
                });
    }

    private int extractNumericPart(String email) {
        String numericPart = email.replaceAll("[^0-9]", "");
        return Integer.parseInt(numericPart);
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
                        .encryptedMessage(Base64.getEncoder().encodeToString(chatMessage.getEncryptedMessageContent()))
                        .iv(Base64.getEncoder().encodeToString(chatMessage.getIv()))
                        .encryptedKeyForRecipient(Base64.getEncoder().encodeToString(chatMessage.getEncryptedKeyForRecipient()))
                        .encryptedKeyForSender(Base64.getEncoder().encodeToString(chatMessage.getEncryptedKeyForSender()))
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
                        .encryptedMessage(lastMessageInfo.getEncryptedMessage())
                        .iv(lastMessageInfo.getIv())
                        .encryptedKeyForRecipient(lastMessageInfo.getEncryptedKeyForRecipient())
                        .encryptedKeyForSender(lastMessageInfo.getEncryptedKeyForSender())
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


    private List<String> extractChatRoomIdsUserChatSettingsMap(Map<String, UserChatSettings> userChatSettingsMap) {
        return userChatSettingsMap.values().stream()
                .map(UserChatSettings::getChatRoomId)
                .toList();
    }

    private UserChatSettingsDTO mapUserChatSettingsDTO(UserChatSettings settings) {
        if (settings == null) return null;
        return IChatMapper.INSTANCE.userChatSettingsToDTO(settings);
    }

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


}
