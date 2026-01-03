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
    // INBOUND FROM WS → CONTACTS
    public static final String CONTACTS_PROFILE_EXCHANGE = "contacts.profile.exchange";
    public static final String CONTACTS_PROFILE_QUEUE = "contacts.profile.queue";
    public static final String CONTACTS_PROFILE_ROUTING = "contacts.profile";

    public static final String CONTACTS_PRIVACY_EXCHANGE = "contacts.privacy.exchange";
    public static final String CONTACTS_PRIVACY_QUEUE = "contacts.privacy.queue";
    public static final String CONTACTS_PRIVACY_ROUTING = "contacts.privacy";

    @Bean
    public TopicExchange contactsProfileExchange() { return new TopicExchange(CONTACTS_PROFILE_EXCHANGE); }

    @Bean
    public Queue contactsProfileQueue() {
        return QueueBuilder.durable(CONTACTS_PROFILE_QUEUE).build();
    }

    @Bean
    public Binding bindContactsProfile() {
        return BindingBuilder.bind(contactsProfileQueue())
                .to(contactsProfileExchange())
                .with(CONTACTS_PROFILE_ROUTING);
    }

    @Bean
    public TopicExchange contactsPrivacyExchange() { return new TopicExchange(CONTACTS_PRIVACY_EXCHANGE); }

    @Bean
    public Queue contactsPrivacyQueue() {
        return QueueBuilder.durable(CONTACTS_PRIVACY_QUEUE).build();
    }

    @Bean
    public Binding bindContactsPrivacy() {
        return BindingBuilder.bind(contactsPrivacyQueue())
                .to(contactsPrivacyExchange())
                .with(CONTACTS_PRIVACY_ROUTING);
    }
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}

