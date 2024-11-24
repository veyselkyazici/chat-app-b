package com.vky.listener;

import com.vky.repository.entity.ChatRoom;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChatRoomModelListener extends AbstractMongoEventListener<ChatRoom> {

//    private final MongoOperations mongoOperations;
//
//    public ChatRoomModelListener(MongoOperations mongoOperations) {
//        this.mongoOperations = mongoOperations;
//    }
//
//    @Override
//    public void onBeforeConvert(BeforeConvertEvent<ChatRoom> event) {
//        super.onBeforeConvert(event);
//        ChatRoom chatRoom = event.getSource();
//
//        List<String> participantIds = chatRoom.getParticipantIds();
//        String userId = participantIds.get(0);
//        String friendId = participantIds.get(1);
//        String id = String.join("_", userId, friendId);
//        chatRoom.setId(id);
//    }
}
