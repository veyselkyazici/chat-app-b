package com.vky.service;

import com.vky.dto.LastMessageInfo;
import com.vky.dto.request.MessageRequestDTO;
import com.vky.dto.response.ChatDTO;
import com.vky.dto.response.MessageDTO;
import com.vky.mapper.IChatMapper;
import com.vky.repository.IChatMessageRepository;
import com.vky.repository.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements IChatMessageService {

    private final IChatMessageRepository chatMessageRepository;

    @Override
    public ChatMessage sendMessage(MessageRequestDTO messageRequestDTO) {
        Instant fullDateTime = Instant.parse(messageRequestDTO.fullDateTime());

        return chatMessageRepository.save(ChatMessage.builder()
                .encryptedMessageContent(Base64.getDecoder().decode(messageRequestDTO.encryptedMessage()))
                .iv(Base64.getDecoder().decode(messageRequestDTO.iv()))
                .encryptedKeyForRecipient(Base64.getDecoder().decode(messageRequestDTO.encryptedKeyForRecipient()))
                .encryptedKeyForSender(Base64.getDecoder().decode(messageRequestDTO.encryptedKeyForSender()))
                .senderId(messageRequestDTO.senderId())
                .recipientId(messageRequestDTO.recipientId())
                .isSeen(false)
                .chatRoomId(messageRequestDTO.chatRoomId())
                .fullDateTime(fullDateTime)
                .build());

    }

    @Override
    public ChatDTO getLast30Messages(String chatRoomId, Pageable pageable, Instant fullDateTime) {
        Instant effectiveDeletedTime = fullDateTime == null ? Instant.EPOCH : fullDateTime;

        List<ChatMessage> chatMessages = chatMessageRepository
                .findTop30ByChatRoomIdAfterDeletedTime(chatRoomId, effectiveDeletedTime, pageable);
        Collections.reverse(chatMessages);
        return getChatDTO(pageable, chatMessages);
    }

    @Override
    public ChatDTO getOlderMessages(String chatRoomId, Instant before, Pageable pageable, Instant fullDateTime) {
        Instant effectiveDeletedTime = fullDateTime == null ? Instant.EPOCH : fullDateTime;

        List<ChatMessage> chatMessages = chatMessageRepository.findNext30ByChatRoomIdAndFullDateTimeBetween(chatRoomId,
                before, effectiveDeletedTime, pageable);
        return getChatDTO(pageable, chatMessages);
    }

    private ChatDTO getChatDTO(Pageable pageable, List<ChatMessage> chatMessages) {
        boolean isLastPage = chatMessages.size() < pageable.getPageSize();

        if (chatMessages.isEmpty()) {
            return ChatDTO.builder()
                    .participantIds(new ArrayList<>())
                    .messages(new ArrayList<>())
                    .isLastPage(true)
                    .build();
        }

        List<String> participantIds = new ArrayList<>();
        participantIds.add(chatMessages.get(0).getSenderId());
        participantIds.add(chatMessages.get(0).getRecipientId());

        return ChatDTO.builder()
                .participantIds(participantIds)
                .messages(IChatMapper.INSTANCE.chatMessagesToDTO(chatMessages))
                .isLastPage(isLastPage)
                .id(chatMessages.get(0).getChatRoomId())
                .build();
    }

    @Override
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
                    lastMessage.isSeen());
            lastMessagesMap.put(lastMessage.getChatRoomId(), lastMessageInfo);
        }
        return lastMessagesMap;
    }

    @Override
    public ChatMessage getLastMessageForChatRooms(String chatRoomId) {
        return chatMessageRepository.findLatestMessageByChatRoomId(chatRoomId);
    }

    @Override
    public List<MessageDTO> setMessagesAsSeen(String chatRoomId, String recipientId, int unreadCount) {

        if (unreadCount <= 0) {
            return Collections.emptyList();
        }

        Pageable pageable = PageRequest.of(0, unreadCount, Sort.by(Sort.Direction.DESC, "fullDateTime"));

        List<ChatMessage> chatMessages = chatMessageRepository
                .findByChatRoomIdAndRecipientIdOrderByFullDateTimeDesc(chatRoomId, recipientId, pageable);

        for (ChatMessage chatMessage : chatMessages) {
            chatMessage.setSeen(true);
        }
        chatMessageRepository.saveAll(chatMessages);
        return IChatMapper.INSTANCE.chatMessagesToDTO(chatMessages);
    }
}
