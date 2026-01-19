package com.vky.service;

import com.vky.config.RabbitMQConfig;
import com.vky.dto.PrivacySettingsResponseDTO;
import com.vky.dto.UpdateSettingsRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PrivacyUpdatedListener {

    private final PrivacyCache privacyCache;
    private final SimpMessagingTemplate messaging;
    private final RedisTemplate<String, Object> redisTemplate;
    private final StatusBroadcastService rel;

    @RabbitListener(queues = RabbitMQConfig.WS_PRIVACY_QUEUE)
    public void onPrivacyUpdated(UpdateSettingsRequestDTO event) {

        privacyCache.put(event.id().toString(), event.privacySettings());

        messaging.convertAndSendToUser(
                event.id().toString(),
                "/queue/updated-privacy-response",
                eventAsUserProfile(event));

        List<String> relatedUsers = rel.getRelatedUsers(event.id().toString());
        if (relatedUsers == null || relatedUsers.isEmpty())
            return;

        for (String viewerId : relatedUsers) {
            if (viewerId == null || viewerId.equals(event.id().toString()))
                continue;

            boolean viewerOnline = Boolean.TRUE.equals(
                    redisTemplate.hasKey("session:" + viewerId));

            if (!viewerOnline)
                continue;

            messaging.convertAndSendToUser(
                    viewerId,
                    "/queue/updated-privacy-response",
                    eventAsUserProfile(event));
        }
    }

    private UpdatedPrivacyResponse eventAsUserProfile(UpdateSettingsRequestDTO event) {
        return new UpdatedPrivacyResponse(event.id().toString(), event.privacySettings());
    }

    public record UpdatedPrivacyResponse(String id, PrivacySettingsResponseDTO privacySettings) {
    }
}