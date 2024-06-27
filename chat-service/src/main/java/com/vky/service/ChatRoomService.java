package com.vky.service;

import com.vky.dto.response.ChatSummaryDTO;
import com.vky.dto.request.MessageRequestDTO;
import com.vky.dto.response.ChatRoomMessageResponseDTO;
import com.vky.dto.response.ChatRoomResponseDTO;
import com.vky.dto.response.ChatRoomWithMessagesDTO;
import com.vky.manager.IUserManager;
import com.vky.mapper.IChatMapper;
import com.vky.repository.IChatRoomRepository;
import com.vky.repository.entity.ChatMessage;
import com.vky.repository.entity.ChatRoom;
import com.vky.repository.entity.UserChatSettings;
import lombok.RequiredArgsConstructor;
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
    public ChatRoomResponseDTO findByParticipantIds(String userId, String friendId) {
        List<String> ids = new ArrayList<>();
        ids.add(userId);
        ids.add(friendId);
        ChatRoom chatRoom = this.chatRoomRepository.findByParticipantIdsContainsAll(ids);
        if (chatRoom != null) {
            List<ChatMessage> messages = chatMessageService.getChatMessages(chatRoom.getId());
            List<ChatRoomMessageResponseDTO> messageDTOs = messages.stream()
                    .map(IChatMapper.INSTANCE::chatMessageToDTO)
                    .collect(Collectors.toList());
            return ChatRoomResponseDTO.builder().messages(messageDTOs).userId(userId).friendId(friendId).id(chatRoom.getId()).build();
        }
        else {
            ChatRoom chatRoomSave = chatRoomSave(userId, friendId);
            userChatSettingsService.saveUserChatSettings(chatRoomSave.getId(), userId);
            userChatSettingsService.saveUserChatSettings(chatRoomSave.getId(), friendId);
            return ChatRoomResponseDTO.builder().id(chatRoomSave.getId()).userId(userId).friendId(friendId).build();
        }
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

    public List<ChatSummaryDTO> getUserChatSummaries(String userId) {
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
                    .messages(null)
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
}
