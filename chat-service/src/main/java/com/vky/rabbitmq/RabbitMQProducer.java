package com.vky.rabbitmq;

import com.vky.config.RabbitMQConfig;
import com.vky.dto.WsEvent;
import com.vky.dto.request.UnreadMessageCountDTO;
import com.vky.dto.response.MessageFriendResponseDTO;
import com.vky.expcetion.ErrorMessage;
import com.vky.repository.entity.UserChatSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publishDeliverEvent(MessageFriendResponseDTO dto) {
        WsEvent<MessageFriendResponseDTO> event =
                WsEvent.delivery(dto.getRecipientId(), dto);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.WS_DELIVERY_EXCHANGE,
                RabbitMQConfig.WS_DELIVERY_ROUTING,
                event
        );
    }

    public void publishReadConfirmation(UnreadMessageCountDTO dto) {
        WsEvent<UnreadMessageCountDTO> event =
                WsEvent.read(dto.getRecipientId(), dto);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.WS_DELIVERY_EXCHANGE,
                RabbitMQConfig.WS_DELIVERY_ROUTING,
                event
        );
    }

    public void publishBlockEvent(String targetUserId, UserChatSettings settings) {
        WsEvent<UserChatSettings> event =
                WsEvent.block(targetUserId, settings);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.WS_DELIVERY_EXCHANGE,
                RabbitMQConfig.WS_DELIVERY_ROUTING,
                event
        );
    }

    public void publishUnblockEvent(String targetUserId, UserChatSettings settings) {
        WsEvent<UserChatSettings> event =
                WsEvent.unblock(targetUserId, settings);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.WS_DELIVERY_EXCHANGE,
                RabbitMQConfig.WS_DELIVERY_ROUTING,
                event
        );
    }

    public void publishErrorEvent(String userId, ErrorMessage error) {
        WsEvent<ErrorMessage> event =
                WsEvent.error(userId, error);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.WS_DELIVERY_EXCHANGE,
                RabbitMQConfig.WS_DELIVERY_ROUTING,
                event
        );
    }
}

