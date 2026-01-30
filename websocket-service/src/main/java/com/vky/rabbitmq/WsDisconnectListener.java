package com.vky.rabbitmq;

import com.vky.config.WebSocketInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WsDisconnectListener {

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onDisconnect(WebSocketInterceptor.WsKickEvent ev) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.create();
        headers.setSessionId(ev.sessionId());
        headers.setLeaveMutable(true);

        messagingTemplate.convertAndSendToUser(
                ev.userId(),
                "/queue/disconnect",
                ev.reason(),
                headers.getMessageHeaders()
        );
    }
}
