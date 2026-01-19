package com.vky.rabbitmq.producer;

import com.vky.config.RabbitConfig;
import com.vky.dto.request.UpdateSettingsRequestDTO;
import com.vky.dto.response.UserProfileResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMQProducer {
    private final RabbitTemplate rabbitTemplate;

    public void checkContactUser(UserProfileResponseDTO dto) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.CONTACT_CHECK_EXCHANGE,
                RabbitConfig.CONTACT_CHECK_ROUTING,
                dto
        );
    }

    public void publishPrivacyUpdated(UpdateSettingsRequestDTO dto) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.WS_PRIVACY_EXCHANGE,
                RabbitConfig.WS_PRIVACY_ROUTING,
                dto
        );
    }

    public void publishProfileUpdated(UpdateSettingsRequestDTO dto) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.WS_PRIVACY_EXCHANGE,
                RabbitConfig.WS_PROFILE_ROUTING,
                dto
        );
    }

    public void privacyUpdated(UpdateSettingsRequestDTO dto) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.WS_PRIVACY_EXCHANGE,
                RabbitConfig.WS_PRIVACY_ROUTING,
                dto
        );
    }
}
