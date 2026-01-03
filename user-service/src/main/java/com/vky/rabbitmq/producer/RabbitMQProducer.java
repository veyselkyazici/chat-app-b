package com.vky.rabbitmq.producer;

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
                "exchange-user",
                "key-user",
                dto
        );
    }
}
