package com.vky.service;

import com.vky.dto.request.FindChatMessagesDTO;
import com.vky.dto.request.MessageRequestDTO;
import com.vky.dto.response.ChatRoomMessageResponseDTO;
import com.vky.dto.response.ChatRoomResponseDTO;
import com.vky.dto.response.MessageFriendResponseDTO;
import com.vky.mapper.IChatMapper;
import com.vky.repository.IChatMessageRepository;
import com.vky.repository.entity.ChatMessage;
import com.vky.repository.entity.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final IChatMessageRepository chatMessageRepository;
    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;

    public void processMessage(MessageRequestDTO messageRequestDTO) {
        Optional<String> optionalChatRoomId = chatRoomService.getChatRoomId(messageRequestDTO.getSenderId(), messageRequestDTO.getRecipientId());
        ChatMessage chatMessage;
        if(optionalChatRoomId.isPresent()) {
            chatMessage = this.chatMessageRepository.save(ChatMessage.builder().message(messageRequestDTO.getMessageContent()).senderId(messageRequestDTO.getSenderId()).recipientId(messageRequestDTO.getRecipientId()).isSeen(false).chatRoomId(optionalChatRoomId.get()).build());
        } else {
            String chatRoomId = chatRoomService.chatRoomSave(messageRequestDTO.getSenderId(), messageRequestDTO.getRecipientId());
            chatMessage = this.chatMessageRepository.save(ChatMessage.builder().message(messageRequestDTO.getMessageContent()).senderId(messageRequestDTO.getSenderId()).recipientId(messageRequestDTO.getRecipientId()).isSeen(false).chatRoomId(chatRoomId).build());
        }
        MessageFriendResponseDTO messageFriendResponseDTO = IChatMapper.INSTANCE.toResponseDTO(chatMessage);
        System.out.println(messageRequestDTO.getRecipientId());
        messagingTemplate.convertAndSendToUser(messageRequestDTO.getRecipientId(),"queue/message-friend-response", messageFriendResponseDTO);
    }

    public List<ChatMessage> findChatMessages(FindChatMessagesDTO findChatMessagesDTO) {
        var chatId = chatRoomService.getChatRoomId(findChatMessagesDTO.getSenderId(), findChatMessagesDTO.getRecipientId());
        return chatId.map(chatMessageRepository::findByChatRoomId).orElse(new ArrayList<>());
    }


    public Map<ChatRoomResponseDTO, List<ChatRoomMessageResponseDTO>> getChatRoomsAndMessagesForUser(String userId) {
        List<ChatRoom> chatRooms = chatRoomService.findBySenderIdOrRecipientId(userId, userId);

        Map<ChatRoomResponseDTO, List<ChatRoomMessageResponseDTO>> chatRoomsAndMessages = new HashMap<>();

        for (ChatRoom chatRoom : chatRooms) {
            List<ChatMessage> messages = chatMessageRepository.findByChatRoomId(chatRoom.getId());
            List<ChatRoomMessageResponseDTO> messageDTOs = messages.stream()
                    .map(IChatMapper.INSTANCE::chatMessageToDTO)
                    .collect(Collectors.toList());
            ChatRoomResponseDTO chatRoomDTO = IChatMapper.INSTANCE.chatRoomToDTO(chatRoom, messageDTOs);
            chatRoomsAndMessages.put(chatRoomDTO, messageDTOs);
        }

        return chatRoomsAndMessages;
    }
}
