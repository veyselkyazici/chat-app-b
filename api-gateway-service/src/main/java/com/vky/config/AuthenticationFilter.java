package com.vky.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Base64;

@Component
public class AuthenticationFilter implements GatewayFilter {

    private final WebClient.Builder webClientBuilder;

    @Autowired
    public AuthenticationFilter(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return errorResponse(exchange);
        }

        return isAuthenticated(authorizationHeader)
                .flatMap(isAuthenticated -> {
                    if (isAuthenticated) {
                        String userId = extractUserIdFromToken(authorizationHeader);

                        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                .header("X-Id", userId)
                                .build();

                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    } else {
                        return errorResponse(exchange);
                    }
                });
    }

    private Mono<Boolean> isAuthenticated(String authorizationHeader) {

        return webClientBuilder.build()
                .get()
                .uri("lb://auth-service/api/v1/auth/authenticate")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .retrieve()
                .bodyToMono(Boolean.class)
                .timeout(Duration.ofSeconds(3))
                .retryWhen(Retry.fixedDelay(1, Duration.ofMillis(200)))
                .onErrorReturn(false);

    }

    private Mono<Void> errorResponse(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    public String extractUserIdFromToken(String authorizationHeader) {

        String token = authorizationHeader.replace("Bearer ", "");

        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT format");
            }

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(payload);

            JsonNode idNode = jsonNode.get("id");
            if (idNode == null || idNode.asText().isBlank()) {
                throw new RuntimeException("ID claim not found in token");
            }

            return idNode.asText();

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JWT", e);
        }
    }
}
