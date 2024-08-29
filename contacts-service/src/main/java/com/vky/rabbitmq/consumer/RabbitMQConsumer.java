package com.vky.rabbitmq.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vky.rabbitmq.model.CheckContactDTO;
import com.vky.repository.entity.Invitation;
import com.vky.service.ContactsService;
import com.vky.service.InvitationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RabbitMQConsumer {
    private  final ContactsService contactsService;
    private final InvitationService invitationService;
    private  final ObjectMapper objectMapper;

    @RabbitListener(queues = "queue-contact-check-user")
    public void checkContactUser(String user){
        log.info("User received: {}", user.toString());
        try {
            CheckContactDTO userObject = objectMapper.readValue(user, CheckContactDTO.class);
            Invitation invitation = invitationService.findByInvitedUserEmailAndIsDeletedFalse(userObject.getEmail());

            if (invitation != null) {
                boolean isExists = contactsService.isExists(invitation.getInviteeEmail(), invitation.getInviterUserId());
                if (!isExists) {
                    contactsService.saveRegisterUserContact(invitation, userObject.getId());
                }
            }
        } catch (JsonProcessingException e) {

        }

    }
}
