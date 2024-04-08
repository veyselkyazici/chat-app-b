package com.vky.listener;

import com.vky.repository.entity.ChatRoom;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
public class ChatRoomModelListener extends AbstractMongoEventListener<ChatRoom> {

    private final MongoOperations mongoOperations;

    public ChatRoomModelListener(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @Override
    public void onBeforeConvert(BeforeConvertEvent<ChatRoom> event) {
        super.onBeforeConvert(event);
        ChatRoom chatRoom = event.getSource();

        String userId = chatRoom.getUserId();
        String friendId = chatRoom.getFriendId();
        String id = String.join("_", userId, friendId);
        chatRoom.setId(id);
    }
}
