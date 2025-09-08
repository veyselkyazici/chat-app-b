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


    @Bean
    DirectExchange exchangeAuth() {
        return new DirectExchange(exchangeNameAuth,true,false);
    }

}





