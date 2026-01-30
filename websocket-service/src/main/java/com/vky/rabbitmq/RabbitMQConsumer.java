package com.vky.rabbitmq;

import com.vky.config.RabbitMQConfig;
import com.vky.dto.RelationshipSyncEvent;
import com.vky.dto.WsEvent;
import com.vky.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RabbitMQConsumer {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final Duration REL_TTL = Duration.ofDays(1);

    private final WebSocketService webSocketService;

    @RabbitListener(queues = RabbitMQConfig.WS_DELIVERY_QUEUE)
    public void handle(WsEvent<?> event) {

        String destination = switch (event.getType()) {
            //case "profile-updated" -> "/queue/updated-user-profile-message";
            case "online-status" -> "/queue/online-status";
            case "disconnect" -> "/queue/disconnect";
            case "contact-added" -> "/queue/add-contact";
            case "contact-added-user" -> "/queue/add-contact-user";
            case "invited-user-joined" -> "/queue/invited-user-joined";
            case "add-invitation" -> "/queue/add-invitation";
            case "delivery" -> "/queue/received-message";
            case "read-recipient" -> "/queue/read-confirmation-recipient";
            case "read-messages" -> "/queue/read-messages";
            case "block" -> "/queue/block";
            case "unblock" -> "/queue/unblock";
            case "error" -> "/queue/error-message";
            default -> "/queue/unknown";
        };

        webSocketService.deliver(
                event.getTargetUserId(),
                destination,
                event.getType(),
                event.getPayload()
        );
    }

    @RabbitListener(queues = RabbitMQConfig.WS_REL_SYNC_QUEUE)
    public void handleRelationshipSync(RelationshipSyncEvent event) {

        String anyKey = "rel:any:" + event.userId();
        redisTemplate.delete(anyKey);

        for (String id : event.relatedUserIds()) {
            redisTemplate.opsForSet().add(anyKey, id);
        }

        redisTemplate.expire(anyKey, REL_TTL);

        String outKey = "rel:out:" + event.userId();
        redisTemplate.delete(outKey);

        for (String id : event.outgoingContactIds()) {
            redisTemplate.opsForSet().add(outKey, id);
        }

        redisTemplate.expire(outKey, REL_TTL);
    }
}
