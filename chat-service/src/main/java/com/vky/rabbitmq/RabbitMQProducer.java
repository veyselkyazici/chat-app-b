package com.vky.rabbitmq;

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
            rabbitTemplate.convertAndSend(RabbitMQConfig.CHAT_EXCHANGE, messageRequestDTO.getChatRoomId(), messageRequestDTO);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send message to RabbitMQ", e);
        }
    }

    public void readMessage(UnreadMessageCountDTO unreadMessageCountDTO) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.MESSAGE_READ_EXCHANGE, RabbitMQConfig.MESSAGE_READ_ROUTING_KEY, unreadMessageCountDTO);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send read message to RabbitMQ", e);
        }
    }

    public void updateUnreadCountToMongo(String chatRoomId, String userId, String contactId, int unreadCount) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.MONGO_UPDATE_EXCHANGE,
                    RabbitMQConfig.MONGO_UPDATE_ROUTING_KEY,
                    new UnreadMessageCountDTO(chatRoomId, userId, contactId, unreadCount)
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to send update message to RabbitMQ", e);
        }
    }
}

