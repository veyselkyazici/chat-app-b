package com.vky.rabbitmq;

import com.vky.dto.request.MessageRequestDTO;
import com.vky.dto.request.UnreadMessageCountDTO;
import com.vky.dto.response.MessageFriendResponseDTO;
import com.vky.mapper.IChatMapper;
import com.vky.repository.entity.ChatMessage;
import com.vky.service.ChatMessageService;
import com.vky.service.UnreadMessageCountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatMessageListener {

    private final ChatMessageService chatMessageService;
    private final UnreadMessageCountService unreadMessageCountService;
    private final RabbitMQProducer producer;

    public void onMessage(MessageRequestDTO dto) {
        try {
            ChatMessage saved = chatMessageService.sendMessage(dto);

            UnreadMessageCountDTO unreadDto = UnreadMessageCountDTO.builder()
                    .chatRoomId(dto.chatRoomId())
                    .recipientId(dto.recipientId())
                    .senderId(dto.senderId())
                    .build();

            int currentCount = unreadMessageCountService.incrementUnreadCount(unreadDto);

            MessageFriendResponseDTO resp = IChatMapper.INSTANCE.toResponseDTO(saved);
            resp = resp.toBuilder().unreadMessageCount(currentCount).build();

            producer.publishDeliverEvent(resp);

        } catch (Exception e) {
            log.error("Error processing chat message", e);
        }
    }
}
