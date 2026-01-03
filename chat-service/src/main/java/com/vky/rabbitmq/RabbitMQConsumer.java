package com.vky.rabbitmq;

import com.vky.config.RabbitMQConfig;
import com.vky.dto.request.MessageRequestDTO;
import com.vky.dto.request.UnreadMessageCountDTO;
import com.vky.dto.response.MessageDTO;
import com.vky.repository.entity.ChatMessage;
import com.vky.service.ChatMessageService;
import com.vky.service.ChatRoomService;
import com.vky.service.UnreadMessageCountService;
import com.vky.service.UserChatSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RabbitMQConsumer {

    private final ChatRoomService chatRoomService;
    private final UnreadMessageCountService unreadMessageCountService;
    private final ChatMessageService chatMessageService;
    private final RabbitMQProducer producer;

    @RabbitListener(queues = RabbitMQConfig.CHAT_INCOMING_QUEUE)
    public void onIncoming(MessageRequestDTO dto) {
        chatRoomService.processMessage(dto);
    }

    @RabbitListener(queues = RabbitMQConfig.CHAT_READ_QUEUE)
    public void onRead(UnreadMessageCountDTO dto) {
        int previousCount = unreadMessageCountService.resetUnreadCount(
                dto.getChatRoomId(),
                dto.getRecipientId()
        );

        chatMessageService.setMessagesAsSeen(
                dto.getChatRoomId(),
                dto.getRecipientId(),
                previousCount
        );

        dto.setUnreadMessageCount(0);

        producer.publishReadConfirmation(dto);
    }
}

