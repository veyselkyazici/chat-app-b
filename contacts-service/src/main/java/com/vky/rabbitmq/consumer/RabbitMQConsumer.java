package com.vky.rabbitmq.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vky.dto.response.UserProfileResponseDTO;
import com.vky.repository.entity.Contacts;
import com.vky.repository.entity.Invitation;
import com.vky.service.ContactsService;
import com.vky.service.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMQConsumer {
    private  final ContactsService contactsService;
    private final InvitationService invitationService;
    private  final ObjectMapper objectMapper;

    @RabbitListener(queues = "queue-contact-check-user")
    public void checkContactUser(String user){
        try {
            UserProfileResponseDTO userProfile = objectMapper.readValue(user, UserProfileResponseDTO.class);
            contactsService.checkUsersWhoInvited(userProfile);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not send CreateUser message", e);
        }

    }
}
