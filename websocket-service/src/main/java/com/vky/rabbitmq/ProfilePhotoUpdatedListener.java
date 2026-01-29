package com.vky.rabbitmq;

import com.vky.config.RabbitMQConfig;
import com.vky.dto.UpdateSettingsRequestDTO;
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
public class ProfilePhotoUpdatedListener {

    private final WebSocketService webSocketService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final StatusBroadcastService statusBroadcastService;
    private final VisibilityPolicy visibilityPolicy;

    @RabbitListener(queues = RabbitMQConfig.WS_PROFILE_QUEUE)
    public void onProfilePhotoUpdated(UpdateSettingsRequestDTO event) {

        String targetId = event.id().toString();
        String newUrl = event.image();

        List<String> relatedUsers = statusBroadcastService.getRelatedUsers(targetId);
        if (relatedUsers == null || relatedUsers.isEmpty()) return;

        for (String viewerId : relatedUsers) {
            if (viewerId == null || viewerId.equals(targetId)) continue;

            boolean viewerOnline = Boolean.TRUE.equals(redisTemplate.hasKey("session:" + viewerId));
            if (!viewerOnline) continue;

            boolean allowed = visibilityPolicy.canSeeProfilePhoto(viewerId, targetId);

            webSocketService.deliver(
                    viewerId,
                    "/queue/updated-user-profile-message",
                    "profile-updated",
                    new UpdatedUserProfileMessage(
                            targetId,
                            allowed ? newUrl : null
                    ));
        }
    }
    public record UpdatedUserProfileMessage(String userId, String url) {}
}
