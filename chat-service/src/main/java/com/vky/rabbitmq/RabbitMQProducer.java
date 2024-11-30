package com.vky.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vky.config.RabbitMQConfig;
import com.vky.dto.request.MessageRequestDTO;
import com.vky.dto.request.UnreadMessageCountDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMQProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendMessage(MessageRequestDTO messageRequestDTO) {
        try {
            String message = new ObjectMapper().writeValueAsString(messageRequestDTO);
            rabbitTemplate.convertAndSend(RabbitMQConfig.CHAT_EXCHANGE, RabbitMQConfig.CHAT_ROUTING_KEY, message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send message to RabbitMQ", e);
        }
    }

    public void readMessage(UnreadMessageCountDTO unreadMessageCountDTO) {
        try {
            String message = new ObjectMapper().writeValueAsString(unreadMessageCountDTO);
            rabbitTemplate.convertAndSend(RabbitMQConfig.CHAT_EXCHANGE, RabbitMQConfig.MESSAGE_READ_ROUTING_KEY, message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send read message to RabbitMQ", e);
        }
    }

    public void updateUnreadCountToMongo(String chatRoomId, String userId, String contactId, int unreadCount) {
        try {
            String unreadMessageCount = new ObjectMapper().writeValueAsString(new UnreadMessageCountDTO(chatRoomId, userId, contactId, unreadCount));
            rabbitTemplate.convertAndSend("mongo-update-queue", unreadMessageCount);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send update message to RabbitMQ", e);
        }
    }
}

