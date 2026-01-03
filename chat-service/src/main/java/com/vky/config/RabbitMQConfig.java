package com.vky.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String CHAT_INCOMING_EXCHANGE = "chat.incoming.exchange";
    public static final String CHAT_INCOMING_QUEUE    = "chat.incoming.queue";
    public static final String CHAT_INCOMING_ROUTING  = "chat.incoming";

    @Bean
    public TopicExchange chatIncomingExchange() {
        return new TopicExchange(CHAT_INCOMING_EXCHANGE, true, false);
    }

    @Bean
    public Queue chatIncomingQueue() {
        return QueueBuilder.durable(CHAT_INCOMING_QUEUE).build();
    }

    @Bean
    public Binding bindChatIncoming() {
        return BindingBuilder.bind(chatIncomingQueue())
                .to(chatIncomingExchange())
                .with(CHAT_INCOMING_ROUTING);
    }

    public static final String CHAT_READ_EXCHANGE = "chat.read.exchange";
    public static final String CHAT_READ_QUEUE    = "chat.read.queue";
    public static final String CHAT_READ_ROUTING  = "chat.read";

    @Bean
    public TopicExchange chatReadExchange() {
        return new TopicExchange(CHAT_READ_EXCHANGE, true, false);
    }

    @Bean
    public Queue chatReadQueue() {
        return QueueBuilder.durable(CHAT_READ_QUEUE).build();
    }

    @Bean
    public Binding bindChatRead() {
        return BindingBuilder.bind(chatReadQueue())
                .to(chatReadExchange())
                .with(CHAT_READ_ROUTING);
    }

    public static final String WS_DELIVERY_EXCHANGE = "ws.delivery.exchange";
    public static final String WS_DELIVERY_ROUTING  = "ws.delivery";

    @Bean
    public TopicExchange wsDeliveryExchange() {
        return new TopicExchange(WS_DELIVERY_EXCHANGE, true, false);
    }

    @Bean
    public MessageConverter jsonConverter(ObjectMapper mapper) {
        return new Jackson2JsonMessageConverter(mapper);
    }
}

