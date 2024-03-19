package com.vky.controller;

import com.vky.dto.request.ChatListRequestDTO;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatMessageService chatMessageService;

    @MessageMapping("/send-message")
    public void sendMessage(@Payload MessageRequestDTO messageRequestDTO) {
        chatMessageService.sendMessage(messageRequestDTO);
    }

    @PostMapping("/find-chat-messages")
    public ResponseEntity<List<ChatMessage>> findChatMessages(@RequestBody FindChatMessagesDTO findChatMessagesDTO) {
        return ResponseEntity
                .ok(chatMessageService.findChatMessages(findChatMessagesDTO));
    }

    @PostMapping("/get-chat-list")
    public ResponseEntity<List<ChatRoomResponseDTO>> getChatList(@RequestBody ChatListRequestDTO chatListRequestDTO) {
        List<ChatRoomResponseDTO> chatRoomResponseDTOs = chatMessageService.getChatList(chatListRequestDTO.getUserId());
        return ResponseEntity.ok(chatRoomResponseDTOs);
    }


}
