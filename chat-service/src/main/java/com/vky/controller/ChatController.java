package com.vky.controller;

import com.vky.dto.request.ChatListRequestDTO;
import com.vky.dto.request.ChatRequestDTO;
import com.vky.dto.request.MessageRequestDTO;
import com.vky.dto.response.ChatRoomResponseDTO;
import com.vky.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatRoomService chatRoomService;

    @MessageMapping("/send-message")
    public void sendMessage(@Payload MessageRequestDTO messageRequestDTO) {
        System.out.println("AAAAAAAAAAAAA" + messageRequestDTO);
        chatRoomService.sendMessage(messageRequestDTO);
    }

    @PostMapping("/get-chat-list")
    public ResponseEntity<List<ChatRoomResponseDTO>> getChatList(@RequestBody ChatListRequestDTO chatListRequestDTO) {
        List<ChatRoomResponseDTO> chatRoomResponseDTOs = chatRoomService.getChatList(chatListRequestDTO.getUserId());
        return ResponseEntity.ok(chatRoomResponseDTOs);
    }

    @PostMapping("/get-chat-message")
    public ResponseEntity<ChatRoomResponseDTO> getChatMessage(@RequestBody ChatRequestDTO chatRequestDTO) {
        ChatRoomResponseDTO chatRoomResponseDTO = chatRoomService.getChatMessage(chatRequestDTO);
        return ResponseEntity.ok(chatRoomResponseDTO);
    }


}
