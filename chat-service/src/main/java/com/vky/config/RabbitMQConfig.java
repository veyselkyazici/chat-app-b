package com.vky.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String CHAT_EXCHANGE = "chat-exchange";
    public static final String MESSAGE_READ_EXCHANGE = "message-read-exchange";
    public static final String MONGO_UPDATE_EXCHANGE = "mongo-update-exchange";

    public static final String CHAT_QUEUE = "chat-queue";
    public static final String CHAT_ROUTING_KEY = "chat.routingKey";

    public static final String MESSAGE_READ_QUEUE = "message-read-queue";
    public static final String MESSAGE_READ_ROUTING_KEY = "message.read.routingKey";

    public static final String MONGO_UPDATE_QUEUE = "mongo-update-queue";
    public static final String MONGO_UPDATE_ROUTING_KEY = "mongo.update.routingKey";

    // Chat Exchange and Queue
    @Bean
    public TopicExchange chatExchange() {
        return new TopicExchange(CHAT_EXCHANGE);
    }

    @Bean
    public Queue chatQueue() {
        return new Queue(CHAT_QUEUE);
    }

    @Bean
    public Binding chatBinding(Queue chatQueue, TopicExchange chatExchange) {
        return BindingBuilder.bind(chatQueue).to(chatExchange).with(CHAT_ROUTING_KEY);
    }

    @Bean
    public TopicExchange messageReadExchange() {
        return new TopicExchange(MESSAGE_READ_EXCHANGE);
    }

    @Bean
    public Queue messageReadQueue() {
        return new Queue(MESSAGE_READ_QUEUE);
    }

    @Bean
    public Binding messageReadBinding(Queue messageReadQueue, TopicExchange messageReadExchange) {
        return BindingBuilder.bind(messageReadQueue).to(messageReadExchange).with(MESSAGE_READ_ROUTING_KEY);
    }

    // Mongo Update Exchange and Queue
    @Bean
    public TopicExchange mongoUpdateExchange() {
        return new TopicExchange(MONGO_UPDATE_EXCHANGE);
    }

    @Bean
    public Queue mongoUpdateQueue() {
        return new Queue(MONGO_UPDATE_QUEUE);
    }

    @Bean
    public Binding mongoUpdateBinding(Queue mongoUpdateQueue, TopicExchange mongoUpdateExchange) {
        return BindingBuilder.bind(mongoUpdateQueue).to(mongoUpdateExchange).with(MONGO_UPDATE_ROUTING_KEY);
    }
}

