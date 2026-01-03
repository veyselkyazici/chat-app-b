package com.vky.rabbitmq;

import com.vky.config.RabbitMQConfig;
import com.vky.dto.MessageRequestDTO;
import com.vky.dto.UnreadMessageCountDTO;
import com.vky.dto.UpdatePrivacySettingsRequestDTO;
import com.vky.dto.UpdatedProfilePhotoRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendToChatIncoming(MessageRequestDTO dto) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CHAT_INCOMING_EXCHANGE,
                RabbitMQConfig.CHAT_INCOMING_ROUTING,
                dto
        );
    }

    public void sendReadEvent(UnreadMessageCountDTO dto) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CHAT_READ_EXCHANGE,
                RabbitMQConfig.CHAT_READ_ROUTING,
                dto
        );
    }

    public void publishPrivacyToContacts(UpdatePrivacySettingsRequestDTO dto) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CONTACTS_PRIVACY_EXCHANGE,
                RabbitMQConfig.CONTACTS_PRIVACY_ROUTING,
                dto
        );
    }

    public void publishProfileToContacts(UpdatedProfilePhotoRequestDTO dto) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CONTACTS_PROFILE_EXCHANGE,
                RabbitMQConfig.CONTACTS_PROFILE_ROUTING,
                dto
        );
    }
}
