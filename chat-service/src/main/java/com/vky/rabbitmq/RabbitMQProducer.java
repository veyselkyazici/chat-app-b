package com.vky.rabbitmq;

import com.vky.config.RabbitMQConfig;
import com.vky.dto.WsEvent;
import com.vky.dto.request.UnreadMessageCountDTO;
import com.vky.dto.response.MessageDTO;
import com.vky.dto.response.MessageFriendResponseDTO;
import com.vky.expcetion.ErrorMessage;
import com.vky.repository.entity.UserChatSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RabbitMQProducer {

        private final RabbitTemplate rabbitTemplate;

        public void publishDeliverEvent(MessageFriendResponseDTO dto) {
                WsEvent<MessageFriendResponseDTO> event = WsEvent.delivery(dto.recipientId(), dto);

                rabbitTemplate.convertAndSend(
                                RabbitMQConfig.WS_DELIVERY_EXCHANGE,
                                RabbitMQConfig.WS_DELIVERY_ROUTING,
                                event);
        }

        public void publishReadConfirmation(UnreadMessageCountDTO dto) {
                WsEvent<UnreadMessageCountDTO> event = WsEvent.readRecipient(dto.recipientId(), dto);

                rabbitTemplate.convertAndSend(
                                RabbitMQConfig.WS_DELIVERY_EXCHANGE,
                                RabbitMQConfig.WS_DELIVERY_ROUTING,
                                event);
        }

        public void publishReadMessages(List<MessageDTO> dto, String targetUserId) {
                WsEvent<List<MessageDTO>> event = WsEvent.readMessages(targetUserId, dto);

                rabbitTemplate.convertAndSend(
                                RabbitMQConfig.WS_DELIVERY_EXCHANGE,
                                RabbitMQConfig.WS_DELIVERY_ROUTING,
                                event);
        }

        public void publishBlockEvent(String targetUserId, UserChatSettings settings) {
                WsEvent<UserChatSettings> event = WsEvent.block(targetUserId, settings);

                rabbitTemplate.convertAndSend(
                                RabbitMQConfig.WS_DELIVERY_EXCHANGE,
                                RabbitMQConfig.WS_DELIVERY_ROUTING,
                                event);
        }

        public void publishUnblockEvent(String targetUserId, UserChatSettings settings) {
                WsEvent<UserChatSettings> event = WsEvent.unblock(targetUserId, settings);

                rabbitTemplate.convertAndSend(
                                RabbitMQConfig.WS_DELIVERY_EXCHANGE,
                                RabbitMQConfig.WS_DELIVERY_ROUTING,
                                event);
        }

        public void publishErrorEvent(String userId, ErrorMessage error) {
                WsEvent<ErrorMessage> event = WsEvent.error(userId, error);

                rabbitTemplate.convertAndSend(
                                RabbitMQConfig.WS_DELIVERY_EXCHANGE,
                                RabbitMQConfig.WS_DELIVERY_ROUTING,
                                event);
        }

}
