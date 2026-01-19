package com.vky.config.rabbitmq;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
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
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}





