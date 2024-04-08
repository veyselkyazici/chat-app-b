package com.vky.service;

import com.vky.dto.request.MessageRequestDTO;
import com.vky.dto.response.MessageFriendResponseDTO;
import com.vky.dto.response.TokenResponseDTO;
import com.vky.mapper.IChatMapper;
import com.vky.repository.IChatMessageRepository;
import com.vky.repository.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final IChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void sendMessage(MessageRequestDTO messageRequestDTO, String chatRoomId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        ChatMessage chatMessage = this.chatMessageRepository.save(ChatMessage.builder()
                .messageContent(messageRequestDTO.getMessageContent())
                .senderId(messageRequestDTO.getSenderId())
                .recipientId(messageRequestDTO.getRecipientId())
                .isSeen(false).chatRoomId(chatRoomId)
                .fullDateTime(LocalDateTime.parse(messageRequestDTO.getFullDateTime(), formatter))
                .build());

        MessageFriendResponseDTO messageFriendResponseDTO = IChatMapper.INSTANCE.toResponseDTO(chatMessage);

        messagingTemplate.convertAndSendToUser(messageRequestDTO.getRecipientId(),"queue/received-message", messageFriendResponseDTO);
    }


    public List<ChatMessage> getChatMessages(String chatRoomId) {
        return chatMessageRepository.findByChatRoomId(chatRoomId);
    }

}
