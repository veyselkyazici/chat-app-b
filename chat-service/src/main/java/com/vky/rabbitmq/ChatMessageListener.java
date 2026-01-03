package com.vky.rabbitmq;

import com.vky.dto.request.MessageRequestDTO;
import com.vky.dto.request.UnreadMessageCountDTO;
import com.vky.dto.response.MessageFriendResponseDTO;
import com.vky.expcetion.ErrorMessage;
import com.vky.mapper.IChatMapper;
import com.vky.repository.entity.ChatMessage;
import com.vky.service.ChatMessageService;
import com.vky.service.UnreadMessageCountService;
import com.vky.service.UserChatSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
                    .chatRoomId(dto.getChatRoomId())
                    .recipientId(dto.getRecipientId())
                    .senderId(dto.getSenderId())
                    .build();

            int currentCount = unreadMessageCountService.incrementUnreadCount(unreadDto);

            MessageFriendResponseDTO resp = IChatMapper.INSTANCE.toResponseDTO(saved);
            resp.setUnreadMessageCount(currentCount);

            producer.publishDeliverEvent(resp);

        } catch (Exception e) {
            log.error("Error processing chat message", e);
        }
    }
}
