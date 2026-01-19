package com.vky.rabbitmq;

import com.vky.config.RabbitMQConfig;
import com.vky.dto.request.MessageRequestDTO;
import com.vky.dto.request.UnreadMessageCountDTO;
import com.vky.dto.response.MessageDTO;
import com.vky.service.ChatMessageService;
import com.vky.service.ChatRoomService;
import com.vky.service.UnreadMessageCountService;
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
                dto.chatRoomId(),
                dto.recipientId());

        List<MessageDTO> messages = chatMessageService.setMessagesAsSeen(
                dto.chatRoomId(),
                dto.recipientId(),
                previousCount);

        dto = dto.toBuilder().unreadMessageCount(0).build();

        producer.publishReadConfirmation(dto);
        producer.publishReadMessages(messages, dto.senderId());
    }
}
