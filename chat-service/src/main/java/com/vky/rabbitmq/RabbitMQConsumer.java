package com.vky.rabbitmq;

import com.vky.config.RabbitMQConfig;
import com.vky.dto.request.MessageRequestDTO;
import com.vky.dto.request.UnreadMessageCountDTO;
import com.vky.service.UnreadMessageCountService;
import com.vky.service.UserChatSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMQConsumer {
    private final SimpMessagingTemplate messagingTemplate;
    private final UnreadMessageCountService unreadMessageCountService;
    private final UserChatSettingsService userChatSettingsService;
    private final ChatMessageListener chatMessageListener;

    @RabbitListener(queues = "chat-shard-1", containerFactory = "chatContainerFactory")
    public void shard1(MessageRequestDTO dto) { chatMessageListener.onMessage(dto); }

    @RabbitListener(queues = "chat-shard-2", containerFactory = "chatContainerFactory")
    public void shard2(MessageRequestDTO dto) { chatMessageListener.onMessage(dto); }

    @RabbitListener(queues = "chat-shard-3", containerFactory = "chatContainerFactory")
    public void shard3(MessageRequestDTO dto) { chatMessageListener.onMessage(dto); }

    @RabbitListener(queues = "chat-shard-4", containerFactory = "chatContainerFactory")
    public void shard4(MessageRequestDTO dto) { chatMessageListener.onMessage(dto); }
//    @RabbitListener(queues = RabbitMQConfig.CHAT_QUEUE,concurrency = "5-10")
//    public void sendMessage(String message) {
//        MessageRequestDTO messageRequestDTO = null;
//        try {
//            messageRequestDTO = objectMapper.readValue(message, MessageRequestDTO.class);
//
//            ChatMessage savedMessage = chatMessageService.sendMessage(messageRequestDTO);
//
//            int currentCount = unreadMessageCountService.incrementUnreadCount(messageRequestDTO.getChatRoomId(), messageRequestDTO.getRecipientId(), messageRequestDTO.getSenderId());
//            MessageFriendResponseDTO messageFriendResponseDTO = IChatMapper.INSTANCE.toResponseDTO(savedMessage);
//            messageFriendResponseDTO.setUnreadMessageCount(currentCount);
//
//            messagingTemplate.convertAndSendToUser(
//                    savedMessage.getRecipientId(),
//                    "/queue/received-message",
//                    messageFriendResponseDTO
//            );
//
//        } catch (Exception e) {
//            chatMessageService.sendErrorNotification(messageRequestDTO, ErrorType.UNEXPECTED_ERROR);
//        }
//    }

    @RabbitListener(queues = RabbitMQConfig.MESSAGE_READ_QUEUE, concurrency = "5-10")
    public void handleReadMessage(UnreadMessageCountDTO readMessageDTO) {
        try {

            int count = unreadMessageCountService.resetUnreadCount(
                    readMessageDTO.getChatRoomId(),
                    readMessageDTO.getRecipientId()
            );

//            chatMessageService.setIsSeenUpdateForUnreadMessageCount(
//                    readMessageDTO.getChatRoomId(),
//                    readMessageDTO.getUserId(),
//                    count
//            );

            messagingTemplate.convertAndSendToUser(
                    readMessageDTO.getRecipientId(),
                    "/queue/read-confirmation-recipient",
                    "Read operation completed for chatRoomId: " + readMessageDTO.getChatRoomId()
            );

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to process read event", e);
        }
    }

    @RabbitListener(queues = "mongo-update-queue", concurrency = "5-10")
    public void updateUnreadCount(UnreadMessageCountDTO  updateDTO  ) {
        try {
            if(updateDTO.getUnreadMessageCount() > 0) {
                userChatSettingsService.incrementUnreadCount(updateDTO.getChatRoomId(), updateDTO.getRecipientId(), updateDTO.getUnreadMessageCount());
            } else {
                userChatSettingsService.resetUnreadCount(updateDTO.getChatRoomId(), updateDTO.getRecipientId(), updateDTO.getUnreadMessageCount());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update MongoDB unread count", e);
        }
    }
}

