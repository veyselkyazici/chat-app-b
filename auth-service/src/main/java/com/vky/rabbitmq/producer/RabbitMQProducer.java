package com.vky.rabbitmq.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vky.rabbitmq.model.CreateUser;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final DirectExchange exchangeAuth;

    public void sendCreateUserMessage(CreateUser user){
        try {
            String jsonUser = objectMapper.writeValueAsString(user);
            rabbitTemplate.convertAndSend(exchangeAuth.getName(),
                    "key-auth", jsonUser);
        }catch (JsonProcessingException e) {
            throw new RuntimeException("Could not send CreateUser message", e);
        }

    }
}
