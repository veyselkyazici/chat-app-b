package com.vky.controller;

import com.vky.dto.request.ChatListRequestDTO;
import com.vky.dto.request.ChatRequestDTO;
import com.vky.dto.request.CreateChatRoom;
import com.vky.dto.request.MessageRequestDTO;
import com.vky.dto.response.ChatRoomResponseDTO;
import com.vky.dto.response.ChatRoomWithMessagesDTO;
import com.vky.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatRoomService chatRoomService;
    @PostMapping("/create-chat-room-if-not-exists")
    public ResponseEntity<ChatRoomResponseDTO> createChatRoomIfNotExists(@RequestBody CreateChatRoom createChatRoom) {
        return ResponseEntity.ok(this.chatRoomService.findByParticipantIds(createChatRoom.getUserId(), createChatRoom.getFriendId()));
    }
    @MessageMapping("/send-message")
    public void handleMessage(@Payload MessageRequestDTO messageRequestDTO) {
        System.out.println("AAAAAAAAAAAAA" + messageRequestDTO);
        chatRoomService.processMessage(messageRequestDTO);
    }
    @GetMapping("/chat-room-with-messages/{userId}")
    public ResponseEntity<List<ChatRoomWithMessagesDTO>> getUserChatRoomsAndMessages(@PathVariable String userId) {
        return ResponseEntity.ok(chatRoomService.getUserChatRoomsAndMessages(userId));
    }

    @GetMapping("/hello")
    public String getUserChatRoomsAndMessages() {
        return "HELLO";
    }

    @PostMapping("/get-chat-list")
    public ResponseEntity<List<ChatRoomResponseDTO>> getChatList(@RequestBody ChatListRequestDTO chatListRequestDTO) {
        List<ChatRoomResponseDTO> chatRoomResponseDTOs = chatRoomService.getChatList(chatListRequestDTO.getUserId());
        return ResponseEntity.ok(chatRoomResponseDTOs);
    }

    @PostMapping("/delete-chat")
    public void deleteChat(){

    }

/*    @PostMapping("/get-chat-message")
    public ResponseEntity<ChatRoomResponseDTO> getChatMessage(@RequestBody ChatRequestDTO chatRequestDTO) {
        ChatRoomResponseDTO chatRoomResponseDTO = chatRoomService.getChatMessage(chatRequestDTO);
        return ResponseEntity.ok(chatRoomResponseDTO);
    }*/


    @PutMapping()
    public ResponseEntity<Boolean> updateUserChatSetting(){
        return null;
    }
}
