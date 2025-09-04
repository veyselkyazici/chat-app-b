package com.vky.service;

import com.vky.dto.LastMessageInfo;
import com.vky.dto.request.MessageRequestDTO;
import com.vky.dto.response.*;
import com.vky.expcetion.ErrorMessage;
import com.vky.expcetion.ErrorType;
import com.vky.mapper.IChatMapper;
import com.vky.repository.IChatMessageRepository;
import com.vky.repository.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final IChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatMessage sendMessage(MessageRequestDTO messageRequestDTO) {
        Instant fullDateTime = Instant.parse(messageRequestDTO.getFullDateTime());

        return chatMessageRepository.save(ChatMessage.builder()
                .encryptedMessageContent(Base64.getDecoder().decode(messageRequestDTO.getEncryptedMessage()))
                .iv(Base64.getDecoder().decode(messageRequestDTO.getIv()))
                .encryptedKeyForRecipient(Base64.getDecoder().decode(messageRequestDTO.getEncryptedKeyForRecipient()))
                .encryptedKeyForSender(Base64.getDecoder().decode(messageRequestDTO.getEncryptedKeyForSender()))
                .senderId(messageRequestDTO.getSenderId())
                .recipientId(messageRequestDTO.getRecipientId())
                .isSeen(false)
                .chatRoomId(messageRequestDTO.getChatRoomId())
                .fullDateTime(fullDateTime)
                .build());

    }

    public void sendErrorNotification(MessageRequestDTO messageRequestDTO, ErrorType errorType) {
        String destination = "/queue/error-message";
        ErrorMessage errorMessage = new ErrorMessage(errorType.getCode(),errorType.getMessage(),null);
        messagingTemplate.convertAndSendToUser(messageRequestDTO.getSenderId(), destination, errorMessage);
    }
    public List<ChatMessage> getChatMessages(String chatRoomId) {
        return chatMessageRepository.findByChatRoomIdAndIsDeletedFalse(chatRoomId);
    }



    public ChatDTO getLast30Messages(String chatRoomId, Pageable pageable, Instant fullDateTime) {
        Instant effectiveDeletedTime = fullDateTime == null ? Instant.EPOCH : fullDateTime;
        List<ChatMessage> chatMessages = chatMessageRepository
                .findTop30ByChatRoomIdAfterDeletedTime(chatRoomId, effectiveDeletedTime, pageable)
                .stream()
                .sorted(Comparator.comparing(ChatMessage::getFullDateTime))
                .toList();
        return getChatDTO(pageable, chatMessages);
    }

    public ChatDTO getOlderMessages(String chatRoomId, Instant before, Pageable pageable) {
        List<ChatMessage> chatMessages = chatMessageRepository.findNext30ByChatRoomIdAndFullDateTimeBefore(chatRoomId, before, pageable);
        return getChatDTO(pageable, chatMessages);
    }

    private ChatDTO getChatDTO(Pageable pageable, List<ChatMessage> chatMessages) {
        boolean isLastPage = chatMessages.size() < pageable.getPageSize();

        ChatDTO chatDTO = new ChatDTO();
        chatDTO.setParticipantIds(new ArrayList<>());
        chatDTO.setMessages(IChatMapper.INSTANCE.chatMessagesToDTO(chatMessages));
        chatDTO.setLastPage(!isLastPage);
        chatDTO.setId(chatMessages.get(0).getChatRoomId());
        chatDTO.getParticipantIds().add(chatMessages.get(0).getSenderId());
        chatDTO.getParticipantIds().add(chatMessages.get(0).getRecipientId());
        return chatDTO;
    }


    public Map<String, LastMessageInfo> getLastMessagesForChatRooms(List<String> chatRoomIds) {
        Map<String, LastMessageInfo> lastMessagesMap = new HashMap<>();

        List<ChatMessage> lastMessages = chatMessageRepository.findLatestMessagesByChatRoomIds(chatRoomIds);

        for (ChatMessage lastMessage : lastMessages) {
            LastMessageInfo lastMessageInfo = new LastMessageInfo(
                    lastMessage.getChatRoomId(),
                    lastMessage.getId(),
                    Base64.getEncoder().encodeToString(lastMessage.getEncryptedMessageContent()),
                    Base64.getEncoder().encodeToString(lastMessage.getIv()),
                    Base64.getEncoder().encodeToString(lastMessage.getEncryptedKeyForRecipient()),
                    Base64.getEncoder().encodeToString(lastMessage.getEncryptedKeyForSender()),
                    lastMessage.getFullDateTime(),
                    lastMessage.getSenderId(),
                    lastMessage.getRecipientId(),
                    lastMessage.isSeen()
            );
            lastMessagesMap.put(lastMessage.getChatRoomId(), lastMessageInfo);
        }
        return lastMessagesMap;
    }
    public ChatMessage getLastMessageForChatRooms(String chatRoomId) {
        return chatMessageRepository.findLatestMessageByChatRoomId(chatRoomId);
    }
    public void setIsSeenUpdateForUnreadMessageCount(String chatRoomId, String userId, int unreadMessageCount) {
        Pageable pageable = PageRequest.of(0, unreadMessageCount, Sort.by(Sort.Direction.DESC, "fullDateTime"));
        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomIdAndRecipientIdOrderByFullDateTimeDesc(chatRoomId, userId,pageable);
        for (ChatMessage chatMessage : chatMessages) {
            chatMessage.setSeen(true);
        }
        List<ChatMessage> chatMessageList = chatMessageRepository.saveAll(chatMessages);
        ChatMessage message = chatMessageList.get(0);
        String senderId = message.getSenderId().equals(userId) ? message.getRecipientId() : message.getSenderId();
        messagingTemplate.convertAndSendToUser(
                senderId,
                "/queue/read-messages",
                chatMessageList
        );
    }


}
