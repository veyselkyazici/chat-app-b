package com.vky.rabbitmq.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vky.rabbitmq.model.CreateUser;
import com.vky.repository.entity.UserProfile;
import com.vky.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

//@Service
//@Slf4j
//@RequiredArgsConstructor
public class CreateUserConsumer {

//    private  final UserProfileService userProfileService;
//    private  final ObjectMapper objectMapper;
//
//    @RabbitListener(queues = "queue-auth-create-user")
//    public void createUserMessageConsumer(String user){
//        log.info("User received: {}", user.toString());
//        try {
//            CreateUser userObject = objectMapper.readValue(user, CreateUser.class);
//            System.out.println("AUTHIDDDDDDDDDDDDDDDDDDD" + userObject.getAuthId());
//            userProfileService.saveUser(UserProfile.builder()
//                    .authId(userObject.getAuthId())
//                    .username(userObject.getUsername())
//                    .email(userObject.getEmail())
//                    .build());
//        } catch (JsonProcessingException e) {
//
//        }
//
//    }
}
