package com.vky.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String WS_DELIVERY_EXCHANGE = "ws.delivery.exchange";
    public static final String WS_DELIVERY_QUEUE    = "ws.delivery.queue";
    public static final String WS_DELIVERY_ROUTING  = "ws.delivery";

    public static final String WS_REL_SYNC_EXCHANGE = "ws.relationship.sync.exchange";
    public static final String WS_REL_SYNC_QUEUE    = "ws.relationship.sync.queue";
    public static final String WS_REL_SYNC_ROUTING  = "ws.relationship.sync";

    public static final String WS_PRIVACY_EXCHANGE = "ws.privacy.exchange";
    public static final String WS_PRIVACY_QUEUE    = "ws.privacy.queue";
    public static final String WS_PRIVACY_ROUTING  = "ws.privacy.routing";

    public static final String WS_PROFILE_ROUTING   = "ws.profile.routing";
    public static final String WS_PROFILE_QUEUE    = "ws.profile.queue";


    @Bean
    public TopicExchange wsPrivacyExchange() {
        return new TopicExchange(WS_PRIVACY_EXCHANGE, true, false);
    }

    @Bean
    public Queue wsPrivacyQueue() {
        return QueueBuilder.durable(WS_PRIVACY_QUEUE).build();
    }

    @Bean
    public Binding bindWsPrivacy() {
        return BindingBuilder.bind(wsPrivacyQueue())
                .to(wsPrivacyExchange())
                .with(WS_PRIVACY_ROUTING);
    }

    @Bean
    public Queue wsProfileQueue() {
        return QueueBuilder.durable(WS_PROFILE_QUEUE).build();
    }

    @Bean
    public Binding bindWsProfile() {
        return BindingBuilder.bind(wsProfileQueue())
                .to(wsPrivacyExchange())
                .with(WS_PROFILE_ROUTING);
    }


    @Bean
    public TopicExchange wsDeliveryExchange() {
        return new TopicExchange(WS_DELIVERY_EXCHANGE, true, false);
    }

    @Bean
    public Queue wsDeliveryQueue() {
        return QueueBuilder.durable(WS_DELIVERY_QUEUE).build();
    }

    @Bean
    public Binding bindWsDelivery() {
        return BindingBuilder.bind(wsDeliveryQueue())
                .to(wsDeliveryExchange())
                .with(WS_DELIVERY_ROUTING);
    }


    @Bean
    public TopicExchange wsRelationshipSyncExchange() {
        return new TopicExchange(WS_REL_SYNC_EXCHANGE, true, false);
    }

    @Bean
    public Queue wsRelationshipSyncQueue() {
        return QueueBuilder.durable(WS_REL_SYNC_QUEUE).build();
    }

    @Bean
    public Binding bindWsRelationshipSync() {
        return BindingBuilder.bind(wsRelationshipSyncQueue())
                .to(wsRelationshipSyncExchange())
                .with(WS_REL_SYNC_ROUTING);
    }

    // CHAT
    public static final String CHAT_INCOMING_EXCHANGE = "chat.incoming.exchange";
    public static final String CHAT_INCOMING_ROUTING  = "chat.incoming";

    public static final String CHAT_READ_EXCHANGE = "chat.read.exchange";
    public static final String CHAT_READ_ROUTING  = "chat.read";

    @Bean
    public TopicExchange chatIncomingExchange() {
        return new TopicExchange(CHAT_INCOMING_EXCHANGE);
    }

    @Bean
    public TopicExchange chatReadExchange() {
        return new TopicExchange(CHAT_READ_EXCHANGE);
    }

    @Bean
    public MessageConverter jsonConverter(ObjectMapper mapper) {
        return new Jackson2JsonMessageConverter(mapper);
    }
}






