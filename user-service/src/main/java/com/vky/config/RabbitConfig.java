package com.vky.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE_AUTH = "exchange-auth";
    public static final String CONTACT_CHECK_EXCHANGE = "exchange-user";
    public static final String CONTACT_CHECK_ROUTING = "key-user";
    public static final String BINDING_KEY_AUTH = "key-auth";
    public static final String QUEUE_AUTH_TO_USER_CREATE = "queue-user-create";


    public static final String WS_PRIVACY_EXCHANGE = "ws.privacy.exchange";
    public static final String WS_PRIVACY_ROUTING   = "ws.privacy.routing";
    public static final String WS_PROFILE_ROUTING   = "ws.profile.routing";


    @Bean
    DirectExchange exchangeAuth() {
        return new DirectExchange(EXCHANGE_AUTH,true,false);
    }
    @Bean
    public DirectExchange exchangeUser() {
        return new DirectExchange(CONTACT_CHECK_EXCHANGE,true,false);
    }

    @Bean
    Queue queueAuthToUserCreate() {
        return new Queue(QUEUE_AUTH_TO_USER_CREATE, true);
    }

    @Bean
    Binding bindingAuthToUserCreate(final Queue queueAuthToUserCreate, final DirectExchange exchangeAuth) {
        return BindingBuilder.bind(queueAuthToUserCreate).to(exchangeAuth).with(BINDING_KEY_AUTH);
    }

    @Bean
    public TopicExchange WsPrivacyExchange() {
        return new TopicExchange(WS_PRIVACY_EXCHANGE);
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
