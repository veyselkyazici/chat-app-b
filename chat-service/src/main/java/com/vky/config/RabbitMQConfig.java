package com.vky.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {
    public static final String CHAT_EXCHANGE = "chat-exchange";
    public static final String MESSAGE_READ_EXCHANGE = "message-read-exchange";
    public static final String MONGO_UPDATE_EXCHANGE = "mongo-update-exchange";


    public static final String MESSAGE_READ_QUEUE = "message-read-queue";
    public static final String MESSAGE_READ_ROUTING_KEY = "message.read.routingKey";

    public static final String MONGO_UPDATE_QUEUE = "mongo-update-queue";
    public static final String MONGO_UPDATE_ROUTING_KEY = "mongo.update.routingKey";


//    @Bean
//    public TopicExchange chatExchange() {
//        return new TopicExchange(CHAT_EXCHANGE, true, false);
//    }
//
    @Bean
    public MessageConverter messageConverter(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean(name = "chatContainerFactory")
    public SimpleRabbitListenerContainerFactory chatContainerFactory(
            org.springframework.amqp.rabbit.connection.ConnectionFactory cf,
            MessageConverter converter) {
        var f = new SimpleRabbitListenerContainerFactory();
        f.setConnectionFactory(cf);
        f.setMessageConverter(converter);
        f.setConcurrentConsumers(1);
        f.setMaxConcurrentConsumers(1);
        f.setPrefetchCount(100);
        f.setDefaultRequeueRejected(false);
        return f;
    }



    @Bean
    public CustomExchange chatExchange() {
        Map<String, Object> args = new HashMap<>();
        return new CustomExchange(CHAT_EXCHANGE, "x-consistent-hash", true, false, args);
    }
    @Bean
    public Queue chatShard1() {
        return QueueBuilder.durable("chat-shard-1").build();
    }

    @Bean
    public Queue chatShard2() {
        return QueueBuilder.durable("chat-shard-2").build();
    }

    @Bean
    public Queue chatShard3() {
        return QueueBuilder.durable("chat-shard-3").build();
    }

    @Bean
    public Queue chatShard4() {
        return QueueBuilder.durable("chat-shard-4").build();
    }
    @Bean
    public Binding bindShard1(CustomExchange chatExchange) {
        return BindingBuilder.bind(chatShard1()).to(chatExchange).with("1").noargs();
    }

    @Bean
    public Binding bindShard2(CustomExchange chatExchange) {
        return BindingBuilder.bind(chatShard2()).to(chatExchange).with("1").noargs();
    }

    @Bean
    public Binding bindShard3(CustomExchange chatExchange) {
        return BindingBuilder.bind(chatShard3()).to(chatExchange).with("1").noargs();
    }

    @Bean
    public Binding bindShard4(CustomExchange chatExchange) {
        return BindingBuilder.bind(chatShard4()).to(chatExchange).with("1").noargs();
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

