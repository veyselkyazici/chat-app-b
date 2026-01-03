package com.vky.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    private String exchangeNameAuth = "exchange-auth";
    private String exchangeUser = "exchange-user";
    private String bindingKeyAuth = "key-auth";
    private String queueAuthToUserCreate = "queue-user-create";


    @Bean
    DirectExchange exchangeAuth() {
        return new DirectExchange(exchangeNameAuth,true,false);
    }
    @Bean
    public DirectExchange exchangeUser() {
        return new DirectExchange(exchangeUser,true,false);
    }

    @Bean
    Queue queueAuthToUserCreate() {
        return new Queue(queueAuthToUserCreate, true);
    }

    @Bean
    Binding bindingAuthToUserCreate(final Queue queueAuthToUserCreate, final DirectExchange exchangeAuth) {
        return BindingBuilder.bind(queueAuthToUserCreate).to(exchangeAuth).with(bindingKeyAuth);
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
