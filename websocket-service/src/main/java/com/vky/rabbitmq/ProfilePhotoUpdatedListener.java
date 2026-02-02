package com.vky.rabbitmq;

import com.vky.config.RabbitMQConfig;
import com.vky.dto.UpdateSettingsRequestDTO;
import com.vky.service.StatusBroadcastService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProfilePhotoUpdatedListener {

    private final StatusBroadcastService statusBroadcastService;

    @RabbitListener(queues = RabbitMQConfig.WS_PROFILE_QUEUE)
    public void onProfilePhotoUpdated(UpdateSettingsRequestDTO event) {

        String targetId = event.id().toString();
        String newUrl = event.image();

        statusBroadcastService.broadcastProfilePhotoChange(targetId, newUrl);
    }

    public record UpdatedUserProfileMessage(String userId, String url) {
    }
}
