package com.vky.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RateLimiterFilter implements GatewayFilter {

    private final ReactiveStringRedisTemplate redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String ip   = getClientIp(exchange.getRequest());

        return isAllowed(path, ip)
                .flatMap(allowed -> {
                    if (!allowed) {
                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        exchange.getResponse().getHeaders().add("Retry-After", "60");
                        return exchange.getResponse().setComplete();
                    }
                    return chain.filter(exchange);
                });
    }

    private Mono<Boolean> isAllowed(String path, String ip) {
        RateLimitRule rule = getRateLimitRule(path);
        String key  = "rate_limit:" + rule.getScope() + ":" + ip;

        return redisTemplate.opsForValue()
                .increment(key)
                .flatMap(current -> redisTemplate.getExpire(key)
                        .flatMap(ttl -> {
                            if (ttl == null || ttl.getSeconds() <= 0) {
                                // TTL yoksa veya 0 ise ayarla
                                return redisTemplate.expire(key, Duration.ofSeconds(rule.getTimeWindow()))
                                        .thenReturn(current);
                            }
                            return Mono.just(current);
                        }))
                .map(current -> current <= rule.getMaxRequests());
    }


    private RateLimitRule getRateLimitRule(String path) {
        if (path.contains("/create-forgot-password")) {
            return new RateLimitRule(6, 180, "sensitive");
        } else if (path.contains("/check-otp")) {
            return new RateLimitRule(6, 180, "sensitive");
        } else if (path.contains("/reset-password")) {
            return new RateLimitRule(6, 180, "sensitive");
        } else if (path.contains("/auth/")) {
            return new RateLimitRule(10, 60, "auth");
        } else if (path.contains("/user/")) {
            return new RateLimitRule(30, 60, "user");
        } else if (path.contains("/contacts/")) {
            return new RateLimitRule(30, 60, "contacts");
        } else if (path.contains("/chat/")) {
            return new RateLimitRule(30, 60, "chat");
        } else {
            return new RateLimitRule(100, 60, "general");
        }
    }

    private String getClientIp(ServerHttpRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP"
        };

        for (String header : headers) {
            String ip = request.getHeaders().getFirst(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddress() != null ?
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    @Data
    @AllArgsConstructor
    private static class RateLimitRule {
        private int maxRequests;
        private int timeWindow;
        private String scope;
    }

}
