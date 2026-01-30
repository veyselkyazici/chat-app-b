package com.vky;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.nio.charset.StandardCharsets;

@Component
public class WebSocketErrorHandler extends StompSubProtocolErrorHandler {
    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {

        WsAuthException authException = findAuthException(ex);
        if (authException != null) {
            return prepareErrorMessage(clientMessage, authException.getCode());
        }
        return super.handleClientMessageProcessingError(clientMessage, ex);
    }
    private WsAuthException findAuthException(Throwable ex) {
        if (ex instanceof WsAuthException) return (WsAuthException) ex;
        if (ex.getCause() != null && ex.getCause() != ex) return findAuthException(ex.getCause());
        return null;
    }
    private Message<byte[]> prepareErrorMessage(Message<byte[]> clientMessage, String errorCode) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);

        accessor.setMessage(errorCode);
        accessor.setLeaveMutable(true);
        if (clientMessage != null) {
            StompHeaderAccessor clientHeader = MessageHeaderAccessor.getAccessor(clientMessage, StompHeaderAccessor.class);
            if (clientHeader != null && clientHeader.getReceipt() != null) {
                accessor.setReceiptId(clientHeader.getReceipt());
            }
        }

        String body = String.format("{\"code\": \"%s\", \"message\": \"%s\"}", errorCode, "Authentication failed");

        return MessageBuilder.createMessage(
                body.getBytes(StandardCharsets.UTF_8),
                accessor.getMessageHeaders()
        );
    }
}
