package com.vky.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);      // Çekirdek thread sayısı
        executor.setMaxPoolSize(10);      // Maksimum thread sayısı
        executor.setQueueCapacity(25);    // Kuyruk kapasitesi
        executor.initialize();
        return executor;
    }
}