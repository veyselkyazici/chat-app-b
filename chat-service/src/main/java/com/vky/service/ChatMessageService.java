package com.vky.service;

import com.vky.dto.LastMessageInfo;
import com.vky.dto.request.CreateChatRoom;
import com.vky.dto.request.CreateChatRoomDTO;
import com.vky.dto.request.MessageRequestDTO;
import com.vky.dto.response.*;
import com.vky.mapper.IChatMapper;
import com.vky.repository.IChatMessageRepository;
import com.vky.repository.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final IChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

//    public void sendMessage(MessageRequestDTO messageRequestDTO, boolean isSuccess) {
//        Instant fullDateTime = Instant.parse(messageRequestDTO.getFullDateTime());
//        ChatMessage chatMessage = chatMessageRepository.save(ChatMessage.builder()
//                .messageContent(messageRequestDTO.getMessageContent())
//                .senderId(messageRequestDTO.getSenderId())
//                .recipientId(messageRequestDTO.getRecipientId())
//                .isSeen(false)
//                .chatRoomId(messageRequestDTO.getChatRoomId())
//                .fullDateTime(fullDateTime)
//                .build());
//
//        MessageFriendResponseDTO messageFriendResponseDTO = IChatMapper.INSTANCE.toResponseDTO(chatMessage);
//        messageFriendResponseDTO.setSuccess(isSuccess);
//
//        String destination = isSuccess ? "queue/received-message" : "/queue/error";
//        messagingTemplate.convertAndSendToUser(
//                isSuccess ? messageRequestDTO.getRecipientId() : messageRequestDTO.getSenderId(),
//                destination,
//                messageFriendResponseDTO);
//
//
//    }
    public ChatMessage sendMessage(MessageRequestDTO messageRequestDTO) {
        Instant fullDateTime = Instant.parse(messageRequestDTO.getFullDateTime());
        return chatMessageRepository.save(ChatMessage.builder()
                .messageContent(messageRequestDTO.getMessageContent())
                .senderId(messageRequestDTO.getSenderId())
                .recipientId(messageRequestDTO.getRecipientId())
                .isSeen(false)
                .chatRoomId(messageRequestDTO.getChatRoomId())
                .fullDateTime(fullDateTime)
                .build());

    }

    public void sendErrorNotification(MessageRequestDTO messageRequestDTO, boolean isSenderBlocked) {
        String destination = "/queue/error";
        String errorMessage = isSenderBlocked
                ? "You are blocked and cannot send messages to this user."
                : "Recipient has blocked you from sending messages.";
        messagingTemplate.convertAndSendToUser(messageRequestDTO.getSenderId(), destination, errorMessage);
    }
    public List<ChatMessage> getChatMessages(String chatRoomId) {
        return chatMessageRepository.findByChatRoomIdAndIsDeletedFalse(chatRoomId);
    }

    public ChatMessage getChatLastMessage(String chatRoomId) {
        return chatMessageRepository.findFirstByChatRoomIdOrderByFullDateTimeDesc(chatRoomId);
    }

    public MessagesDTO getLast30Messages(String chatRoomId, Pageable pageable) {
        List<ChatMessage> chatMessages = chatMessageRepository.findTop30ByChatRoomId(chatRoomId, pageable).stream()
                .sorted(Comparator.comparing(ChatMessage::getFullDateTime))
                .toList();
        boolean isLastPage = chatMessages.size() < pageable.getPageSize();
        List<ChatRoomMessageResponseDTO> messages = IChatMapper.INSTANCE.chatMessagesToDTO(chatMessages);
        return new MessagesDTO(messages, isLastPage);

    }

    public MessagesDTO getOlderMessages(String chatRoomId, Instant before, Pageable pageable) {
        List<ChatMessage> chatMessages = chatMessageRepository.findNext30ByChatRoomIdAndFullDateTimeBefore(chatRoomId, before, pageable);
        boolean isLastPage = chatMessages.size() < pageable.getPageSize();
        List<ChatRoomMessageResponseDTO> messages = IChatMapper.INSTANCE.chatMessagesToDTO(chatMessages);
        return new MessagesDTO(messages, isLastPage);
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

    public Map<String, LastMessageInfo> getLastMessagesForChatRooms(List<String> chatRoomIds) {
        Map<String, LastMessageInfo> lastMessagesMap = new HashMap<>();

        List<ChatMessage> lastMessages = chatMessageRepository.findFirstByChatRoomIdInOrderByFullDateTimeDesc(chatRoomIds);

        for (ChatMessage lastMessage : lastMessages) {
            LastMessageInfo lastMessageInfo = new LastMessageInfo(
                    lastMessage.getChatRoomId(),
                    lastMessage.getMessageContent(),
                    lastMessage.getFullDateTime(),
                    lastMessage.getSenderId(),
                    lastMessage.getRecipientId()
            );
            lastMessagesMap.put(lastMessage.getChatRoomId(), lastMessageInfo);
        }
        return lastMessagesMap;
    }

    public void setIsSeenUpdateForUnreadMessageCount(String chatRoomId, String userId, int unreadMessageCount) {
        Pageable pageable = PageRequest.of(0, unreadMessageCount, Sort.by(Sort.Direction.DESC, "fullDateTime"));
        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomIdAndRecipientIdOrderByFullDateTimeDesc(chatRoomId, userId,pageable);
        for (ChatMessage chatMessage : chatMessages) {
            chatMessage.setSeen(true);
        }
        chatMessageRepository.saveAll(chatMessages);

    }


}
