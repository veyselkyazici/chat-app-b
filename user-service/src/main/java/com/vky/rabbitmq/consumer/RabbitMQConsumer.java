package com.vky.rabbitmq.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vky.dto.request.NewUserCreateDTO;
import com.vky.rabbitmq.model.CreateUser;
import com.vky.repository.entity.UserProfile;
import com.vky.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RabbitMQConsumer {

    private  final UserProfileService userProfileService;
    private  final ObjectMapper objectMapper;

    @RabbitListener(queues = "queue-user-create")
    public void createUserMessageConsumer(String user){
        log.info("User received: {}", user.toString());
        try {
            CreateUser userObject = objectMapper.readValue(user, CreateUser.class);
            userProfileService.createUserProfile(userObject);
        } catch (JsonProcessingException e) {

        }

    }
}
