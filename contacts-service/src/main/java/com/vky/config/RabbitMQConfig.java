package com.vky.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String CONTACT_CHECK_EXCHANGE = "exchange-user";
    public static final String CONTACT_CHECK_QUEUE = "queue-contact-check-user";
    public static final String CONTACT_CHECK_ROUTING = "key-user";

    @Bean
    public DirectExchange contactCheckExchange() {
        return new DirectExchange(CONTACT_CHECK_EXCHANGE, true, false);
    }

    @Bean
    public Queue contactCheckQueue() {
        return QueueBuilder.durable(CONTACT_CHECK_QUEUE).build();
    }

    @Bean
    public Binding bindContactCheck() {
        return BindingBuilder
                .bind(contactCheckQueue())
                .to(contactCheckExchange())
                .with(CONTACT_CHECK_ROUTING);
    }
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}

