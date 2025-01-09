package com.vky.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Value("${raceapplication.url.user}")
    private String contactsBaseUrl;
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.baseUrl(contactsBaseUrl + "api/v1/user").build();
    }
}
