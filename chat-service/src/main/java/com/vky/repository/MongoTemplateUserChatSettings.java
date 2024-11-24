package com.vky.repository;

import com.mongodb.client.result.UpdateResult;
import com.vky.repository.entity.UserChatSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
@RequiredArgsConstructor
public class MongoTemplateUserChatSettings implements IMongoTemplateUserChatSettings {
    private final MongoTemplate mongoTemplate;
    @Override
    public void updateIncrementUnreadMessageCount(String chatRoomId, String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("chatRoomId").is(chatRoomId)
                .and("userId").is(userId));

        Update update = new Update();
        update.inc("unreadMessageCount", 1);
        update.set("updatedAt", Instant.now());

        UpdateResult result = mongoTemplate.updateFirst(query, update, UserChatSettings.class);

        if (result.getModifiedCount() == 0) {
            throw new RuntimeException("Güncelleme yapılamadı, kayıt bulunamadı!");
        }
    }

    @Override
    public void updateResetUnreadMessageCount(String chatRoomId, String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("chatRoomId").is(chatRoomId)
                .and("userId").is(userId));
        Update update = new Update();
        update.set("unreadMessageCount", 0);
        update.set("updatedAt", Instant.now());

        UpdateResult result = mongoTemplate.updateFirst(query, update, UserChatSettings.class);

        if (result.getModifiedCount() == 0) {
            throw new RuntimeException("Güncelleme yapılamadı, kayıt bulunamadı!");
        }
    }
}
