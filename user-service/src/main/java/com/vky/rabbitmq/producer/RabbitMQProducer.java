package com.vky.rabbitmq.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vky.rabbitmq.model.CreateUser;
import com.vky.repository.entity.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMQProducer {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void checkContactUser(UserProfile user){
        try {
            String jsonUser = objectMapper.writeValueAsString(user);
            rabbitTemplate.convertAndSend("exchange-user",
                    "key-user", jsonUser);
        }catch (JsonProcessingException e) {

        }

    }
}