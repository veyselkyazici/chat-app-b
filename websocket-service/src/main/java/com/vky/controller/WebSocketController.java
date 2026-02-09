package com.vky.controller;

import com.vky.dto.MessageRequestDTO;
import com.vky.dto.TypingMessage;
import com.vky.dto.UnreadMessageCountDTO;
import com.vky.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/ws")
public class WebSocketController {

    private final WebSocketService webSocketService;
    private static final int MAX_ENCRYPTED_MESSAGE_LENGTH = 6000;
    @MessageMapping("/send-message")
    public void sendMessage(@Payload MessageRequestDTO dto, Principal principal) {
        if (principal == null)
            return;

        if (dto.encryptedMessage() != null && dto.encryptedMessage().length() > MAX_ENCRYPTED_MESSAGE_LENGTH) {
            return;
        }

        dto = dto.toBuilder().senderId(principal.getName()).build();
        webSocketService.sendMessage(dto);
    }

    @MessageMapping("/read-message")
    public void readMessage(@Payload UnreadMessageCountDTO dto, Principal principal) {
        if (principal == null)
            return;
        dto = dto.toBuilder().recipientId(principal.getName()).build();
        webSocketService.readMessage(dto);
    }

    @MessageMapping("/typing")
    public void typing(@Payload TypingMessage dto, Principal principal) {
        if (principal == null)
            return;
        dto = dto.toBuilder().userId(principal.getName()).build();
        webSocketService.setTyping(dto);
    }

    @MessageMapping("/ping")
    public void ping(Principal principal) {
        if (principal == null)
            return;
        webSocketService.ping("session:" + principal.getName());
    }

    @MessageMapping("/sync")
    public void sync(Principal principal) {
        if (principal == null)
            return;
        webSocketService.syncToUser(principal.getName());
    }

    public record AckRequest(String eventId) {
    }

    @MessageMapping("/ack")
    public void ack(@Payload AckRequest req, Principal principal) {
        if (principal == null || req == null || req.eventId() == null)
            return;
        webSocketService.ack(principal.getName(), req.eventId());
    }

    // @MessageMapping("/updated-privacy-send-message")
    // public void sendPrivacyUpdate(@Payload UpdateSettingsDTO dto,
    // Principal principal) {
    //
    // dto.setId(UUID.fromString(principal.getName()));
    //
    // webSocketService.sendUpdatedPrivacy(dto);
    // }
    //
    // @MessageMapping("/updated-user-profile-send-message")
    // public void sendProfileUpdate(@Payload UpdatedProfilePhotoRequestDTO dto,
    // Principal principal) {
    //
    // dto.setUserId(UUID.fromString(principal.getName()));
    //
    // webSocketService.sendUpdatedProfile(dto);
    // }
}
