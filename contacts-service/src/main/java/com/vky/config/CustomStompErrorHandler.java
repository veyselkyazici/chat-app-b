package com.vky.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vky.exception.ErrorMessage;
import com.vky.exception.ErrorType;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.util.Collections;

@Component
public class CustomStompErrorHandler extends StompSubProtocolErrorHandler {

    private final SimpMessagingTemplate messagingTemplate;

    public CustomStompErrorHandler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public Message<byte[]> handleClientMessageProcessingError(
            Message<byte[]> clientMessage, Throwable ex) {

        ErrorMessage errorMessage = ErrorMessage.builder()
                .code(ErrorType.INTERNAL_ERROR.getCode())
                .message(ex.getMessage())
                .fields(Collections.emptyList())
                .build();

        try {
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(clientMessage);
            String user = accessor.getUser() != null ? accessor.getUser().getName() : null;
            if (user != null) {
                messagingTemplate.convertAndSendToUser(user, "/queue/error", errorMessage);
            }
        } catch (Exception ignore) {}

        return super.handleClientMessageProcessingError(clientMessage, ex);
    }
}
