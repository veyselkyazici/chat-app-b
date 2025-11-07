package com.vky.rabbitmq;

import com.vky.controller.TypingMessage;
import com.vky.dto.request.MessageRequestDTO;
import com.vky.dto.response.MessageFriendResponseDTO;
import com.vky.mapper.IChatMapper;
import com.vky.repository.entity.ChatMessage;
import com.vky.service.ChatMessageService;
import com.vky.service.UnreadMessageCountService;
import com.vky.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatMessageListener {

    private final ChatMessageService chatMessageService;
    private final UnreadMessageCountService unreadMessageCountService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserStatusService userStatusService;

    public void onMessage(MessageRequestDTO dto) {
        try {
            ChatMessage saved = chatMessageService.sendMessage(dto);

            int currentCount = unreadMessageCountService.incrementUnreadCount(
                    dto.getChatRoomId(), dto.getRecipientId(), dto.getSenderId());

            MessageFriendResponseDTO resp = IChatMapper.INSTANCE.toResponseDTO(saved);
            resp.setUnreadMessageCount(currentCount);

            messagingTemplate.convertAndSendToUser(
                    saved.getRecipientId(),
                    "/queue/received-message",
                    resp
            );
            //userStatusService.setTyping(new TypingMessage(dto.getSenderId(), dto.getRecipientId(), dto.getChatRoomId(), false));
        } catch (Exception e) {
            log.error("Error processing chat message. chatRoomId={} sender={} recipient={}",
                    dto.getChatRoomId(), dto.getSenderId(), dto.getRecipientId(), e);
            // TODO DLQ/Retry
        }
    }
}