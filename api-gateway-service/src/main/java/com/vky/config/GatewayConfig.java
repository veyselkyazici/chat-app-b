package com.vky.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfig {
    @Autowired
    private WebClient.Builder webClientBuilder;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        System.out.println("aaaaaaaaaaaaaa");
        return builder.routes()
                .route("auth-service", r -> r.path("/api/v1/auth/**")
                        .uri("lb://auth-service"))
                .route("user-service", r -> r.path("/api/v1/user/**")
                        .filters(f -> f.filter(customFilter()))
                        .uri("lb://user-service"))
                .route("friendships-service", r -> r.path("/api/v1/friendships/**")
                        .filters(f -> f.filter(customFilter()))
                        .uri("lb://friendships-service"))
                .route("chat-service", r -> r.path("/api/v1/chat/**")
                        .filters(f -> f.filter(customFilter()))
                        .uri("lb://chat-service"))
                .build();
    }
    @Bean
    public GatewayFilter customFilter() {
        return (exchange, chain) -> {

            String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            Boolean isAuthenticated = webClientBuilder.build()
                    .get()
                    .uri("http://auth-service/api/v1/auth/authenticate")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

            System.out.println("isAuthenticated: " + isAuthenticated);

            if (isAuthenticated != null && isAuthenticated) {

                System.out.println("User is authenticated.");
              //  exchange.getResponse().getHeaders().add("Access-Control-Allow-Origin", "*");
                return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                    System.out.println("Custom filter for user-service: After the request is executed");
                }));
            } else {

                System.out.println("User is not authenticated.");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

        };
    }
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.addAllowedOrigin("*"); // Güvenlik nedeniyle, özel origin'leri belirtmeniz önerilir
        corsConfig.addAllowedMethod("*");
        corsConfig.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}

