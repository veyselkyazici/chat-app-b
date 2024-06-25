package com.vky.service;

import com.vky.dto.request.MessageRequestDTO;
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

}
