package com.vky.rabbitmq;

import com.vky.config.RabbitMQConfig;
import com.vky.dto.response.UserProfileResponseDTO;
import com.vky.service.ContactsService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMQConsumer {

    private final ContactsService contactsService;

    @RabbitListener(queues = RabbitMQConfig.CONTACT_CHECK_QUEUE)
    public void checkContactUser(UserProfileResponseDTO dto) {
        contactsService.checkUsersWhoInvited(dto);
    }
}

