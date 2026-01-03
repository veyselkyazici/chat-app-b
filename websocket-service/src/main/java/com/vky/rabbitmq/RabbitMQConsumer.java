package com.vky.rabbitmq;

import com.vky.config.RabbitMQConfig;
import com.vky.dto.RelationshipSyncEvent;
import com.vky.dto.WsEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMQConsumer {

    private final SimpMessagingTemplate ws;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String REL_KEY_PREFIX = "rel:";

    @RabbitListener(queues = RabbitMQConfig.WS_DELIVERY_QUEUE)
    public void handle(WsEvent<?> event) {

        String destination = switch (event.getType()) {

            // CONTACTS
            case "privacy-updated" -> "/queue/updated-privacy-response";
            case "profile-updated" -> "/queue/updated-user-profile-message";
            case "online-status"   -> "/queue/online-status";
            case "disconnect"      -> "/queue/disconnect";
            case "contact-added"   -> "/queue/add-contact";
            case "contact-added-user" -> "/queue/add-contact-user";
            case "invited-user-joined" -> "/queue/invited-user-joined";
            case "add-invitation" -> "/queue/add-invitation";

            // CHAT
            case "delivery" -> "/queue/received-message";
            case "read"     -> "/queue/read-confirmation";
            case "block"    -> "/queue/block";
            case "unblock"  -> "/queue/unblock";
            case "error"    -> "/queue/error-message";

            default -> "/queue/unknown";
        };

        ws.convertAndSendToUser(
                event.getTargetUserId(),
                destination,
                event.getPayload()
        );
    }
    @RabbitListener(queues = RabbitMQConfig.WS_REL_SYNC_QUEUE)
    public void handleRelationshipSync(RelationshipSyncEvent event) {

        String key = REL_KEY_PREFIX + event.getUserId();

        redisTemplate.delete(key);

        for (String relatedUserId : event.getRelatedUserIds()) {
            redisTemplate.opsForSet().add(key, relatedUserId);
        }

        // İsteğe bağlı TTL
        // redisTemplate.expire(key, Duration.ofHours(24));
    }
}

