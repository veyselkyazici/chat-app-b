package com.vky.config;

import com.vky.service.TokenBlacklistService;
import com.vky.util.JwtTokenManager;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final RateLimiterFilter rateLimiterFilter;
    private final JwtTokenManager jwtTokenManager;
    private final TokenBlacklistService tokenBlacklistService;
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/api/v1/auth/authenticate")
                        .filters(f -> f.filter(rateLimiterFilter).filter(jwtAuthenticationGatewayFilter()))
                        .uri("lb://auth-service"))
                .route("auth-service", r -> r.path("/api/v1/auth/**")
                        .filters(f -> f.filter(rateLimiterFilter))
                        .uri("lb://auth-service"))
                .route("user-service", r -> r.path("/api/v1/user/**")
                        .filters(f -> f.filter(rateLimiterFilter).filter(jwtAuthenticationGatewayFilter()))
                        .uri("lb://user-service"))
                .route("mail-service", r -> r.path("/api/v1/mail/**")
                        .filters(f -> f.filter(rateLimiterFilter))
                        .uri("lb://mail-service"))
                .route("contacts-service", r -> r.path("/api/v1/contacts/**")
                        .filters(f -> f.filter(rateLimiterFilter).filter(jwtAuthenticationGatewayFilter()))
                        .uri("lb://contacts-service"))
                .route("contacts-service-invitation", r -> r.path("/api/v1/invitation/**")
                        .filters(f -> f.filter(rateLimiterFilter).filter(jwtAuthenticationGatewayFilter()))
                        .uri("lb://contacts-service"))
                .route("chat-service", r -> r.path("/api/v1/chat/**")
                        .filters(f -> f.filter(rateLimiterFilter).filter(jwtAuthenticationGatewayFilter()))
                        .uri("lb://chat-service"))
                .route("chat-service-status", r -> r.path("/api/v1/status/**")
                        .filters(f -> f.filter(rateLimiterFilter).filter(jwtAuthenticationGatewayFilter()))
                        .uri("lb://chat-service"))
                .route("chat-service-ws-http", r -> r.path("/ws/chat/**")
                        .uri("lb://chat-service"))
                .route("contacts-service-ws-http", r -> r.path("/ws/contacts/**")
                        .uri("lb://contacts-service"))
                .build();
    }
    @Bean
    public GatewayFilter jwtAuthenticationGatewayFilter() {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                response.getHeaders().add("Content-Type", "application/json");

                DataBufferFactory bufferFactory = response.bufferFactory();
                DataBuffer buffer = bufferFactory.wrap("{\"error\":\"Authorization header missing\"}".getBytes());
                return response.writeWith(Mono.just(buffer));
            }

            String token = authHeader.substring(7);
            boolean isBlackListed = tokenBlacklistService.isBlacklisted(token);
            try {
                if (!jwtTokenManager.isValidToken(token) || isBlackListed) {
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    response.getHeaders().add("Content-Type", "application/json");

                    DataBufferFactory bufferFactory = response.bufferFactory();
                    DataBuffer buffer = bufferFactory.wrap("{\"error\":\"Invalid token\"}".getBytes());
                    return response.writeWith(Mono.just(buffer));
                }

                String userId = jwtTokenManager.extractAuthId(token).toString();

                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header("X-Id", userId)
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (Exception e) {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                response.getHeaders().add("Content-Type", "application/json");

                DataBufferFactory bufferFactory = response.bufferFactory();
                DataBuffer buffer = bufferFactory.wrap("{\"error\":\"Token validation failed\"}".getBytes());
                return response.writeWith(Mono.just(buffer));
            }
        };
    }
    @Bean
    public CorsWebFilter corsWebFilter() {

        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.addAllowedOrigin("https://vkychatapp.com");
        corsConfig.addAllowedOrigin("https://www.vkychatapp.com");
        corsConfig.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        corsConfig.setAllowedHeaders(List.of("*"));
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}

//@Configuration
//@RequiredArgsConstructor
//public class GatewayConfig {
//
//    private final AuthenticationFilter authenticationFilter;
//    private final RateLimiterFilter rateLimiterFilter;
//    @Bean
//    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
//        return builder.routes()
//                .route("auth-service", r -> r.path("/api/v1/auth/**")
//                        .filters(f -> f.filter(rateLimiterFilter))
//                        .uri("lb://auth-service"))
//                .route("user-service", r -> r.path("/api/v1/user/**")
//                        .filters(f -> f.filter(rateLimiterFilter))
//                        .uri("lb://user-service"))
//                .route("contacts-service", r -> r.path("/api/v1/contacts/**")
//                        .filters(f -> f.filter(rateLimiterFilter).filter(authenticationFilter))
//                        .uri("lb://contacts-service"))
//                .route("contacts-service-invitation", r -> r.path("/api/v1/invitation/**")
//                        .filters(f -> f.filter(rateLimiterFilter).filter(authenticationFilter))
//                        .uri("lb://contacts-service"))
//                .route("chat-service", r -> r.path("/api/v1/chat/**")
//                        .filters(f -> f.filter(rateLimiterFilter).filter(authenticationFilter))
//                        .uri("lb://chat-service"))
//                .route("chat-service-status", r -> r.path("/api/v1/status/**")
//                        .filters(f -> f.filter(rateLimiterFilter).filter(authenticationFilter))
//                        .uri("lb://chat-service"))
//                .route("chat-service-ws-http", r -> r.path("/ws/chat/**")
//                        .uri("lb://chat-service"))
//                .route("contacts-service-ws-http", r -> r.path("/ws/contacts/**")
//                        .uri("lb://contacts-service"))
//                .build();
//    }
//
//    @Bean
//    public CorsWebFilter corsWebFilter() {
//        CorsConfiguration corsConfig = new CorsConfiguration();
//        corsConfig.addAllowedOrigin("http://localhost:3000");
//        corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        corsConfig.setAllowedHeaders(List.of("*"));
//        corsConfig.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/api/**", corsConfig);
//
//        return new CorsWebFilter(source);
//    }
//
//}



