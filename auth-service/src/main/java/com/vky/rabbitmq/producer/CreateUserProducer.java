package com.vky.rabbitmq.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vky.rabbitmq.model.CreateUser;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateUserProducer {
    /**
     * Mesaj iletmek için rabbit template kullanıyoruz.
     */
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void sendCreateUserMessage(CreateUser user){
        try {
            String jsonUser = objectMapper.writeValueAsString(user);
            rabbitTemplate.convertAndSend("exchange-auth",
                    "key-auth", jsonUser);
        }catch (JsonProcessingException e) {

        }

    }
}
