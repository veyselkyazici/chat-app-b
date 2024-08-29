package com.vky.config.rabbitmq;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    /**
     * Exchange türleri > Direct, Fanout, Topic
     * Direct > Routing key ile doğrudan kuyruk yönlendirmesi yapar.
     * Fanout > Routing key e bakmadan tüm bağlı kuyruklara mesajları yönlendirir
     * Topic > Routing keyler belirli bir desenle eşleşen tüm kuyruklara mesajı yönlendirir.
     * */
    private String exchangeNameAuth = "exchange-auth";
    private String exchangeNameUser = "exchange-user";
    private String bindingKeyAuth = "key-auth";
    private String bindingKeyUser = "key-user";
    private String queueAuthToUserCreate = "queue-user-create";
    private String queueContactCheckUser = "queue-contact-check-user";
    @Bean
    DirectExchange exchangeAuth(){
        return new DirectExchange(exchangeNameAuth);
    }
    @Bean
    DirectExchange exchangeUser(){
        return new DirectExchange(exchangeNameUser);
    }
    @Bean
    Queue queueAuthToUserCreate(){
        return new Queue(queueAuthToUserCreate);
    }
    @Bean
    Queue queueContactCheckUser(){
        return new Queue(queueContactCheckUser);
    }
    @Bean
    public Binding bindingAuthToUserCreate(final Queue queueAuthToUserCreate, final DirectExchange exchangeAuth){
        return BindingBuilder.bind(queueAuthToUserCreate).to(exchangeAuth).with(bindingKeyAuth);
    }
    @Bean
    public Binding bindingContactCheckUser(final Queue queueContactCheckUser, final DirectExchange exchangeUser){
        return BindingBuilder.bind(queueContactCheckUser).to(exchangeUser).with(bindingKeyUser);
    }

}





