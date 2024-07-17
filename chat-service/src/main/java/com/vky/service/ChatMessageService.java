package com.vky.service;

import com.vky.dto.request.CreateChatRoom;
import com.vky.dto.request.CreateChatRoomDTO;
import com.vky.dto.request.MessageRequestDTO;
import com.vky.dto.response.ChatRoomMessageResponseDTO;
import com.vky.dto.response.ChatRoomResponseDTO;
import com.vky.dto.response.MessageFriendResponseDTO;
import com.vky.mapper.IChatMapper;
import com.vky.repository.IChatMessageRepository;
import com.vky.repository.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final IChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void sendMessage(MessageRequestDTO messageRequestDTO, boolean isSuccess) {
            Instant fullDateTime = Instant.parse(messageRequestDTO.getFullDateTime());
            ChatMessage chatMessage = chatMessageRepository.save(ChatMessage.builder()
                    .messageContent(messageRequestDTO.getMessageContent())
                    .senderId(messageRequestDTO.getSenderId())
                    .recipientId(messageRequestDTO.getRecipientId())
                    .isSeen(false)
                    .chatRoomId(messageRequestDTO.getChatRoomId())
                    .fullDateTime(fullDateTime)
                    .build());

            MessageFriendResponseDTO messageFriendResponseDTO = IChatMapper.INSTANCE.toResponseDTO(chatMessage);
            messageFriendResponseDTO.setSuccess(isSuccess);

            String destination = isSuccess ? "queue/received-message" : "/queue/error";
            messagingTemplate.convertAndSendToUser(
                    isSuccess ? messageRequestDTO.getRecipientId() : messageRequestDTO.getSenderId(),
                    destination,
                    messageFriendResponseDTO);


    }


    public List<ChatMessage> getChatMessages(String chatRoomId) {
        return chatMessageRepository.findByChatRoomIdAndIsDeletedFalse(chatRoomId);
    }

    public ChatMessage getChatLastMessage(String chatRoomId) {
        return chatMessageRepository.findFirstByChatRoomIdOrderByFullDateTimeDesc(chatRoomId);
    }

    public List<ChatRoomMessageResponseDTO> getLatestMessages(String chatRoomId) {
        List<ChatMessage> chatMessages = chatMessageRepository.findTop30ByChatRoomIdOrderByFullDateTimeDesc(chatRoomId);

        List<ChatRoomMessageResponseDTO> messageDTOs = chatMessages.stream()
                .map(IChatMapper.INSTANCE::chatMessageToDTO)
                .toList();

        return messageDTOs;
    }

    public List<ChatRoomMessageResponseDTO> getOlderMessages(String chatRoomId, Instant before) {
        List<ChatMessage> chatMessages = chatMessageRepository.findNext30ByChatRoomIdAndFullDateTimeBeforeOrderByFullDateTimeDesc(chatRoomId, before);

        List<ChatRoomMessageResponseDTO> messageDTOs = chatMessages.stream()
                .map(IChatMapper.INSTANCE::chatMessageToDTO)
                .toList();

        return messageDTOs;
    }

    public ChatMessage sendFirstMessage(CreateChatRoomDTO createChatRoomDTO, String chatRoomId) {
        Instant fullDateTime = Instant.parse(createChatRoomDTO.getLastMessageTime());
        return chatMessageRepository.save(ChatMessage.builder()
                .messageContent(createChatRoomDTO.getLastMessage())
                .senderId(createChatRoomDTO.getUserId())
                .recipientId(createChatRoomDTO.getFriendId())
                .isSeen(false)
                .chatRoomId(chatRoomId)
                .fullDateTime(fullDateTime)
                .build());
    }
}
