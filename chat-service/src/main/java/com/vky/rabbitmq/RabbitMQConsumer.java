package com.vky.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vky.config.RabbitMQConfig;
import com.vky.dto.request.MessageRequestDTO;
import com.vky.dto.request.UnreadMessageCountDTO;
import com.vky.dto.response.MessageFriendResponseDTO;
import com.vky.mapper.IChatMapper;
import com.vky.repository.entity.ChatMessage;
import com.vky.service.ChatMessageService;
import com.vky.service.UnreadMessageCountService;
import com.vky.service.UserChatSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMQConsumer {
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UnreadMessageCountService unreadMessageCountService;
    private final UserChatSettingsService userChatSettingsService;

    @RabbitListener(queues = RabbitMQConfig.CHAT_QUEUE,concurrency = "5-10")
    public void sendMessage(String message) {
        try {
            MessageRequestDTO messageRequestDTO = new ObjectMapper().readValue(message, MessageRequestDTO.class);

            ChatMessage savedMessage = chatMessageService.sendMessage(messageRequestDTO);

            int currentCount = unreadMessageCountService.incrementUnreadCount(messageRequestDTO.getChatRoomId(), messageRequestDTO.getRecipientId(), messageRequestDTO.getSenderId());
            MessageFriendResponseDTO messageFriendResponseDTO = IChatMapper.INSTANCE.toResponseDTO(savedMessage);
            messageFriendResponseDTO.setUnreadMessageCount(currentCount);

            messagingTemplate.convertAndSendToUser(
                    savedMessage.getRecipientId(),
                    "/queue/received-message",
                    messageFriendResponseDTO
            );

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to process message from RabbitMQ", e);
        }
    }
    @RabbitListener(queues = RabbitMQConfig.MESSAGE_READ_QUEUE, concurrency = "5-10")
    public void handleReadMessage(String message) {
        try {
            UnreadMessageCountDTO readMessageDTO = new ObjectMapper().readValue(message, UnreadMessageCountDTO.class);

            int count = unreadMessageCountService.resetUnreadCount(
                    readMessageDTO.getChatRoomId(),
                    readMessageDTO.getRecipientId(),
                    readMessageDTO.getSenderId()
            );

//            chatMessageService.setIsSeenUpdateForUnreadMessageCount(
//                    readMessageDTO.getChatRoomId(),
//                    readMessageDTO.getUserId(),
//                    count
//            );

            messagingTemplate.convertAndSendToUser(
                    readMessageDTO.getRecipientId(),
                    "/queue/read-confirmation-recipient",
                    "Read operation completed for chatRoomId: " + readMessageDTO.getChatRoomId()
            );

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to process read event from RabbitMQ", e);
        }
    }

    @RabbitListener(queues = "mongo-update-queue", concurrency = "5-10")
    public void updateUnreadCount(String message) {
        try {
            UnreadMessageCountDTO updateDTO = new ObjectMapper().readValue(message, UnreadMessageCountDTO.class);
            if(updateDTO.getUnreadMessageCount() > 0) {
                userChatSettingsService.incrementUnreadCount(updateDTO.getChatRoomId(), updateDTO.getRecipientId(), updateDTO.getUnreadMessageCount());
            } else {
                userChatSettingsService.resetUnreadCount(updateDTO.getChatRoomId(), updateDTO.getRecipientId(), updateDTO.getUnreadMessageCount());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update MongoDB unread count", e);
        }
    }
}

