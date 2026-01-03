package com.vky.rabbitmq;

import com.vky.dto.RelationshipSyncEvent;
import com.vky.dto.WsEvent;
import com.vky.dto.request.UpdatePrivacySettingsRequestDTO;
import com.vky.dto.request.UpdatedProfilePhotoRequestDTO;
import com.vky.dto.response.ContactResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    private void publish(String type, String targetUserId, Object payload) {

        WsEvent<Object> event = WsEvent.of(type, targetUserId, payload);

        rabbitTemplate.convertAndSend(
                "ws.delivery.exchange",
                "ws.delivery",
                event
        );
    }

    public void publishPrivacy(UpdatePrivacySettingsRequestDTO dto, UUID targetUserId) {
        publish("privacy-updated", targetUserId.toString(), dto);
    }

    public void publishProfile(UpdatedProfilePhotoRequestDTO dto, UUID targetUserId) {
        publish("profile-updated", targetUserId.toString(), dto);
    }

    public void publishContactAdded(ContactResponseDTO dto, String targetUserId) {
        publish("contact-added", targetUserId, dto);
    }

    public void publishContactAddedUser(ContactResponseDTO dto, String targetUserId) {
        publish("contact-added-user", targetUserId, dto);
    }

    public void publishInvitedUserJoined(ContactResponseDTO dto, String targetUserId) {
        publish("invited-user-joined", targetUserId, dto);
    }
    public void publishAddInvitation(ContactResponseDTO dto, String targetUserId) {
        publish("add-invitation", targetUserId, dto);
    }

    public void publishRelationshipSync(RelationshipSyncEvent event) {
        rabbitTemplate.convertAndSend(
                "ws.relationship.sync.exchange",
                "ws.relationship.sync",
                event
        );
    }
}

