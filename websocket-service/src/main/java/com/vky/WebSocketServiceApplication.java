package com.vky;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRabbit
public class WebSocketServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebSocketServiceApplication.class, args);
    }
}