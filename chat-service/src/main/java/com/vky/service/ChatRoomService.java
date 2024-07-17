package com.vky.service;

import com.vky.dto.request.CreateChatRoom;
import com.vky.dto.request.CreateChatRoomDTO;
import com.vky.dto.response.*;
import com.vky.dto.request.MessageRequestDTO;
import com.vky.manager.IUserManager;
import com.vky.mapper.IChatMapper;
import com.vky.repository.IChatRoomRepository;
import com.vky.repository.entity.ChatMessage;
import com.vky.repository.entity.ChatRoom;
import com.vky.repository.entity.UserChatSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final IChatRoomRepository chatRoomRepository;
    private final ChatMessageService chatMessageService;
    private final IUserManager userManager;
    private final UserChatSettingsService userChatSettingsService;
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
        System.out.println("USERID > " + userId);

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
            System.out.println(" FRIENDID > " + friendId);
            System.out.println(" USERID > " + userId);
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
        ChatRoom chatRoom = this.chatRoomRepository.findByParticipantIdsContainsAll(ids);
        if (chatRoom != null) {
            UserChatSettings userChatSettings = userChatSettingsService.findByUserIdAndChatRoomId(userId, chatRoom.getId());
            return ChatRoomWithUserChatSettingsDTO.builder().userId(userId).friendId(friendId).userChatSettings(userChatSettings).id(chatRoom.getId()).build();
        }
        else {
            ChatRoom chatRoomSave = chatRoomSave(userId, friendId);
            UserChatSettings userChatSettings = userChatSettingsService.saveUserChatSettings(chatRoomSave.getId(), userId);
            userChatSettingsService.saveUserChatSettings(chatRoomSave.getId(), friendId);
            return ChatRoomWithUserChatSettingsDTO.builder().id(chatRoomSave.getId()).userId(userId).friendId(friendId).userChatSettings(userChatSettings).build();
        }
    }

    public CheckChatRoomExistsResponseDTO checkChatRoomExists(String userId, String friendId) {
        List<String> ids = new ArrayList<>();
        ids.add(userId);
        ids.add(friendId);
        ChatRoom chatRoom = this.chatRoomRepository.findByParticipantIdsContainsAll(ids);
        CheckChatRoomExistsResponseDTO checkChatRoomExistsResponseDTO = new CheckChatRoomExistsResponseDTO();
        if (chatRoom != null) {
            checkChatRoomExistsResponseDTO.setId(chatRoom.getId());
            checkChatRoomExistsResponseDTO.setExists(true);
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

    public void processMessage(MessageRequestDTO messageRequestDTO) {
        boolean isSenderBlocked = isUserBlocked(messageRequestDTO.getSenderId(), messageRequestDTO.getChatRoomId());
        boolean isRecipientBlocked = isUserBlocked(messageRequestDTO.getRecipientId(), messageRequestDTO.getChatRoomId());

        boolean isSuccess = !(isSenderBlocked || isRecipientBlocked);
        chatMessageService.sendMessage(messageRequestDTO, isSuccess);
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

    public List<ChatSummaryDTO> getUserChatSummariess(String userId) {
        List<ChatRoom> chatRooms = getUserChatRooms(userId);

        return chatRooms.stream().map(chatRoom -> {
            List<String> filteredParticipantIds = chatRoom.getParticipantIds()
                    .stream()
                    .filter(id -> !id.equals(userId))
                    .toList();

            String friendId = filteredParticipantIds.isEmpty() ? null : filteredParticipantIds.get(0);
            ChatMessage chatMessage = getChatLastMessage(chatRoom.getId());

            UserChatSettings userChatSettings = userChatSettingsService.findByUserIdAndChatRoomId(userId, chatRoom.getId());

            String friendEmail = friendId != null ? this.userManager.getUserEmailById(UUID.fromString(friendId)) : null;

            return ChatSummaryDTO.builder()
                    .id(chatRoom.getId())
                    .image(null)
                    .lastMessage(chatMessage.getMessageContent())
                    .lastMessageTime(chatMessage.getFullDateTime())
                    .userChatSettings(userChatSettings)
                    .friendEmail(friendEmail)
                    .friendId(friendId)
                    .userId(userId)
                    .build();
        }).collect(Collectors.toList());
    }


    public List<ChatRoomMessageResponseDTO> getLatestMessages(String chatRoomId) {
        return chatMessageService.getLatestMessages(chatRoomId);
    }

    public List<ChatRoomMessageResponseDTO> getOlderMessages(String chatRoomId, Instant before) {
        return chatMessageService.getOlderMessages(chatRoomId, before);
    }
    public boolean chatBlock(ChatSummaryDTO chatSummaryDTO) {
        try {
            // Engelleme işlemi
            UserChatSettings userSettings = this.userChatSettingsService.findByUserIdAndChatRoomId(chatSummaryDTO.getUserId(), chatSummaryDTO.getId());
            userSettings.setBlocked(true);
            userSettings.setBlockedTime(Instant.now());
            userChatSettingsService.updateUserChatSettings(userSettings);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean chatUnblock(ChatSummaryDTO chatSummaryDTO) {
       try {
            // Engelin kaldırılması işlemi
            UserChatSettings userSettings = this.userChatSettingsService.findByUserIdAndChatRoomId(chatSummaryDTO.getUserId(), chatSummaryDTO.getId());
            userSettings.setBlocked(false);
            userSettings.setUnblockedTime(Instant.now());
            userChatSettingsService.updateUserChatSettings(userSettings);
            return true;
        } catch (Exception e) {
            return false;
        }
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
