package com.vky.service;

import com.vky.dto.request.ChatRequestDTO;
import com.vky.dto.request.MessageRequestDTO;
import com.vky.dto.response.ChatRoomMessageResponseDTO;
import com.vky.dto.response.ChatRoomResponseDTO;
import com.vky.dto.response.TokenResponseDTO;
import com.vky.manager.IUserManager;
import com.vky.mapper.IChatMapper;
import com.vky.repository.IChatRoomRepository;
import com.vky.repository.entity.ChatMessage;
import com.vky.repository.entity.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final IChatRoomRepository chatRoomRepository;
    private final ChatMessageService chatMessageService;
    private final IUserManager userManager;

    public ChatRoom getChatRoomId(String userId, String friendId) {
        return chatRoomRepository.findByUserIdAndFriendIdOrFriendIdAndUserId(userId, friendId, userId, friendId);
    }


    public ChatRoom chatRoomSave(String userId, String friendId) {
        return this.chatRoomRepository.save(ChatRoom.builder().userId(userId).friendId(friendId).build());
    }

    public List<ChatRoomResponseDTO> getChatList(String userId) {
        List<ChatRoom> chatRooms = this.chatRoomRepository.findByUserIdOrFriendId(userId, userId);
        List<ChatRoomResponseDTO> chatRoomResponseDTOs = new ArrayList<>();

        for (ChatRoom chatRoom : chatRooms) {
            List<ChatMessage> messages = chatMessageService.getChatMessages(chatRoom.getId());
            List<ChatRoomMessageResponseDTO> messageDTOs = messages.stream()
                    .map(IChatMapper.INSTANCE::chatMessageToDTO)
                    .collect(Collectors.toList());
            ChatRoomResponseDTO chatRoomDTO = ChatRoomResponseDTO.builder().messages(messageDTOs).userId(chatRoom.getUserId()).friendId(chatRoom.getFriendId()).id(chatRoom.getId()).build();
            String newFriendId = chatRoom.getUserId().equals(userId) ? chatRoom.getFriendId() : chatRoom.getUserId();
            UUID friendUUID = UUID.fromString(newFriendId);
            String friendEmail = this.userManager.getUserEmailById(friendUUID);
            chatRoomDTO.setFriendEmail(friendEmail);
            chatRoomResponseDTOs.add(chatRoomDTO);
        }

        return chatRoomResponseDTOs;
    }

    public ChatRoomResponseDTO getChatMessage(ChatRequestDTO chatRequestDTO) {
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
    }


    public void sendMessage(MessageRequestDTO messageRequestDTO) {
        //TokenResponseDTO tokenResponseDTO = userManager.feignClientGetUserId(messageRequestDTO.getSenderToken());

        ChatRoom chatRoom = getChatRoomId(messageRequestDTO.getSenderId(), messageRequestDTO.getRecipientId());
        if(chatRoom == null) {
             chatRoom = chatRoomSave(messageRequestDTO.getSenderId(), messageRequestDTO.getRecipientId());
        }
        this.chatMessageService.sendMessage(messageRequestDTO, chatRoom.getId());
    }


}
