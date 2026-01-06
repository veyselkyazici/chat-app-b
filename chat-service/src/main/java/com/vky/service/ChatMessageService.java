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
public class ChatMessageService {
    private final IChatMessageRepository chatMessageRepository;

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

    public ChatDTO getLast30Messages(String chatRoomId, Pageable pageable, Instant fullDateTime) {
        Instant effectiveDeletedTime = fullDateTime == null ? Instant.EPOCH : fullDateTime;

        List<ChatMessage> chatMessages = chatMessageRepository
                .findTop30ByChatRoomIdAfterDeletedTime(chatRoomId, effectiveDeletedTime, pageable);
        return getChatDTO(pageable, chatMessages);
    }

    public ChatDTO getOlderMessages(String chatRoomId, Instant before, Pageable pageable) {
        List<ChatMessage> chatMessages = chatMessageRepository.findNext30ByChatRoomIdAndFullDateTimeBefore(chatRoomId, before, pageable);
        return getChatDTO(pageable, chatMessages);
    }

    private ChatDTO getChatDTO(Pageable pageable, List<ChatMessage> chatMessages) {
        boolean isLastPage = chatMessages.size() < pageable.getPageSize();

        ChatDTO chatDTO = new ChatDTO();
        if(!chatMessages.isEmpty()) {
            chatDTO.setParticipantIds(new ArrayList<>());
            chatDTO.setMessages(IChatMapper.INSTANCE.chatMessagesToDTO(chatMessages));
            chatDTO.setLastPage(!isLastPage);
            chatDTO.setId(chatMessages.get(0).getChatRoomId());
            chatDTO.getParticipantIds().add(chatMessages.get(0).getSenderId());
            chatDTO.getParticipantIds().add(chatMessages.get(0).getRecipientId());
        }

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
//    public void markUnreadMessagesAsSeen(String chatRoomId, String recipientId, int unreadMessageCount) {
//        if (unreadMessageCount <= 0) return;
//
//        // MongoDB: okunmamış mesajları sırala ve limit kadar al
//        Query query = new Query();
//        query.addCriteria(Criteria.where("chatRoomId").is(chatRoomId)
//                .and("recipientId").is(recipientId)
//                .and("isSeen").is(false));
//        query.with(Sort.by(Sort.Direction.ASC, "fullDateTime")); // eski mesajlar önce
//        query.limit(unreadMessageCount);
//
//        List<ChatMessage> messagesToMark = mongoTemplate.find(query, ChatMessage.class);
//        if (messagesToMark.isEmpty()) return;
//
//        // MongoDB: seçilen mesajları isSeen=true olarak güncelle
//        List<String> ids = messagesToMark.stream().map(ChatMessage::getId).toList();
//        Query updateQuery = new Query(Criteria.where("id").in(ids));
//        Update update = new Update().set("isSeen", true);
//        mongoTemplate.updateMulti(updateQuery, update, ChatMessage.class);
//
//        // Redis: unread message count sıfırla
//        String redisKey = String.format("unread:%s:%s", chatRoomId, recipientId);
//        redisTemplate.opsForValue().set(redisKey, 0, REDIS_TTL);
//
//        // WebSocket: karşı tarafa okunma bilgisini gönder
//        if (!messagesToMark.isEmpty()) {
//            String senderId = messagesToMark.get(0).getSenderId().equals(recipientId)
//                    ? messagesToMark.get(0).getRecipientId()
//                    : messagesToMark.get(0).getSenderId();
//
//            messagingTemplate.convertAndSendToUser( recipientId, "/queue/read-confirmation-recipient", "Read operation completed for chatRoomId: ");
//            messagingTemplate.convertAndSendToUser(
//                    senderId,
//                    "/queue/read-messages",
//                    messagesToMark
//            );
//
//        }
//    }


}
