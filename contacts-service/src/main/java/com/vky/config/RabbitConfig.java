package com.vky.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    private String queueContactCheckUser = "queue-contact-check-user";
    private String exchangeUser = "exchange-user";
    private String bindingKeyUser = "key-user";



    @Bean
    public DirectExchange exchangeUser() {
        return new DirectExchange(exchangeUser,true,false);
    }

    @Bean
    Binding bindingContactCheckUser(final Queue queueContactCheckUser, final DirectExchange exchangeUser){
        return BindingBuilder.bind(queueContactCheckUser).to(exchangeUser).with(bindingKeyUser);
    }

    @Bean
    Queue queueContactCheckUser(){
        return new Queue(queueContactCheckUser, true);
    }

}
