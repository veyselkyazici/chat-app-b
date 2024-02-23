package com.vky.controller;

import com.vky.dto.request.FindChatMessagesDTO;
import com.vky.dto.request.MessageRequestDTO;
import com.vky.dto.response.ChatRoomMessageResponseDTO;
import com.vky.dto.response.ChatRoomResponseDTO;
import com.vky.repository.entity.ChatMessage;
import com.vky.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@CrossOrigin(originPatterns = ("*"))
public class ChatController {

    private final ChatMessageService chatMessageService;

    @MessageMapping("/send-message")
    public void processMessage(@Payload MessageRequestDTO messageRequestDTO) {
        chatMessageService.processMessage(messageRequestDTO);
    }

    @PostMapping("/find-chat-messages")
    public ResponseEntity<List<ChatMessage>> findChatMessages(@RequestBody FindChatMessagesDTO findChatMessagesDTO) {
        return ResponseEntity
                .ok(chatMessageService.findChatMessages(findChatMessagesDTO));
    }

    @PostMapping("/get-chat-rooms-and-messages-for-user")
    public Map<ChatRoomResponseDTO, List<ChatRoomMessageResponseDTO>> getChatRoomsAndMessagesForUser(@RequestBody String userId) {
        return ResponseEntity.ok(chatMessageService.getChatRoomsAndMessagesForUser(userId)).getBody();
    }
}
