package com.vky.rabbitmq;

import com.vky.config.RabbitMQConfig;
import com.vky.dto.PrivacySettingsResponseDTO;
import com.vky.dto.UpdateSettingsDTO;
import com.vky.dto.enums.PrivacyField;
import com.vky.service.PrivacyCache;
import com.vky.service.StatusBroadcastService;
import com.vky.service.VisibilityPolicy;
import com.vky.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PrivacyUpdatedListener {

    private final PrivacyCache privacyCache;
    private final RedisTemplate<String, Object> redisTemplate;
    private final StatusBroadcastService statusBroadcastService;
    private final WebSocketService webSocketService;
    private final VisibilityPolicy visibilityPolicy;

    @RabbitListener(queues = RabbitMQConfig.WS_PRIVACY_QUEUE)
    public void onPrivacyUpdated(UpdateSettingsDTO event) {

        privacyCache.put(event.id().toString(), event.privacySettings());

        List<String> relatedUsers = statusBroadcastService.getRelatedUsers(event.id().toString());
        if (relatedUsers == null || relatedUsers.isEmpty())
            return;

        for (String viewerId : relatedUsers) {
            if (viewerId == null || viewerId.equals(event.id().toString()))
                continue;

            boolean viewerOnline = redisTemplate.hasKey("session:" + viewerId);

            if (!viewerOnline)
                continue;

            webSocketService.deliver(
                    viewerId,
                    "/queue/updated-privacy-response",
                    "privacy-updated",
                    eventAsUserProfile(event));

            if (event.privacy() == PrivacyField.PROFILE_PHOTO) {
                boolean allowed = visibilityPolicy.canSeeProfilePhoto(viewerId, event.id().toString());
                webSocketService.deliver(
                        viewerId,
                        "/queue/updated-user-profile-message",
                        "profile-updated",
                        new ProfilePhotoUpdatedListener.UpdatedUserProfileMessage(
                                event.id().toString(),
                                allowed ? event.image() : null));
            }
        }
    }

    private UpdatedPrivacyResponse eventAsUserProfile(UpdateSettingsDTO event) {
        return new UpdatedPrivacyResponse(event.id().toString(), event.privacySettings());
    }

    public record UpdatedPrivacyResponse(String id, PrivacySettingsResponseDTO privacySettings) {
    }
}