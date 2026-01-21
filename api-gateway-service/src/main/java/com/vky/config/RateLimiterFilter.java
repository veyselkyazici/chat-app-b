package com.vky.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RateLimiterFilter implements GatewayFilter {

    private final ReactiveStringRedisTemplate redisTemplate;

    /**
     * Token Bucket - Lua
     *
     * Girdi Parametreleri:
     *   KEYS[1] = baseKey
     *             (örn: rate_limit:auth:check_otp:1.2.3.4)
     *
     *   ARGV[1] = capacity
     *             Kovanın maksimum token kapasitesi (burst limiti)
     *
     *   ARGV[2] = windowMs
     *             Kovanın 0'dan capacity değerine
     *             kaç milisaniyede tamamen dolacağını belirtir
     *
     *   ARGV[3] = nowMs
     *             Şu anki zaman (epoch milisaniye)
     *
     * Çıktı:
     *   {allowed, tokensLeft, retryAfterSeconds}
     *
     *   allowed:
     *     1 = isteğe izin verildi
     *     0 = rate limit aşıldı (429)
     *
     *   tokensLeft:
     *     İstekten sonra kovada kalan token sayısı
     *     (ondalıklı olabilir)
     *
     *   retryAfterSeconds:
     *     Yeni bir isteğin kabul edilebilmesi için
     *     kaç saniye beklenmesi gerektiği
     */
    private static final RedisScript<List> TOKEN_BUCKET_SCRIPT = new DefaultRedisScript<>(

            """
            local baseKey  = KEYS[1]
            local capacity = tonumber(ARGV[1])
            local windowMs = tonumber(ARGV[2])
            local nowMs    = tonumber(ARGV[3])

            local tokensKey = baseKey .. ":tokens"
            local tsKey     = baseKey .. ":ts"

            -- refill rate in tokens per millisecond
            local rate = capacity / windowMs

            local tokens = tonumber(redis.call("GET", tokensKey))
            local lastTs = tonumber(redis.call("GET", tsKey))

            if tokens == nil then tokens = capacity end
            if lastTs == nil then lastTs = nowMs end

            local delta = nowMs - lastTs
            if delta < 0 then delta = 0 end

            -- smooth refill
            tokens = math.min(capacity, tokens + (delta * rate))
            lastTs = nowMs

            local allowed = 0
            local retryAfterSec = 0

            if tokens >= 1 then
              tokens = tokens - 1
              allowed = 1
            else
              -- seconds until we reach 1 token
              local need = 1 - tokens
              local msToWait = math.ceil(need / rate)
              retryAfterSec = math.ceil(msToWait / 1000)
              allowed = 0
            end

            -- write back
            redis.call("SET", tokensKey, tokens)
            redis.call("SET", tsKey, lastTs)

            -- keep state long enough (2 windows)
            local ttlMs = windowMs * 2
            redis.call("PEXPIRE", tokensKey, ttlMs)
            redis.call("PEXPIRE", tsKey, ttlMs)

            return {allowed, tokens, retryAfterSec}
            """,
            List.class
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String ip   = getClientIp(exchange.getRequest());

        RateLimitRule rule = getRateLimitRule(path);

        String key = String.format("rate_limit:%s:%s:%s", rule.getScope(), rule.getKeySuffix(), ip);

        long capacity = rule.getMaxRequests();
        long windowMs = rule.getTimeWindowSeconds() * 1000L;
        long nowMs    = System.currentTimeMillis();

        return redisTemplate
                .execute(
                        TOKEN_BUCKET_SCRIPT,
                        Collections.singletonList(key),
                        String.valueOf(capacity),
                        String.valueOf(windowMs),
                        String.valueOf(nowMs)
                )
                .next()
                .flatMap(result -> {
                    long allowed = ((Number) result.get(0)).longValue();
                    long retryAfterSec = ((Number) result.get(2)).longValue();

                    if (allowed != 1) {
                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        exchange.getResponse().getHeaders().set("Retry-After", String.valueOf(retryAfterSec));
                        return exchange.getResponse().setComplete();
                    }
                    return chain.filter(exchange);
                });
    }

    /**
     * spesifik olanlar önce, genel olanlar sonra.
     * resend-confirmation mail içerisinde -> scope="mail"
     */
    private RateLimitRule getRateLimitRule(String path) {

        // AUTH - sensitive (spesifik)
        if (path.startsWith("/api/v1/auth/create-forgot-password")) {
            return new RateLimitRule(6, 180, "auth", "forgot_password");
        }
        if (path.startsWith("/api/v1/auth/check-otp")) {
            return new RateLimitRule(6, 180, "auth", "check_otp");
        }
        if (path.startsWith("/api/v1/auth/reset-password")) {
            return new RateLimitRule(6, 180, "auth", "reset_password");
        }

        // MAIL - resend-confirmation (spesifik)
        if (path.startsWith("/api/v1/mail/resend-confirmation")) {
            return new RateLimitRule(3, 60, "mail", "resend_confirmation");
        }

        // AUTH - general
        if (path.startsWith("/api/v1/auth/")) {
            return new RateLimitRule(10, 60, "auth", "general");
        }

        // USER / CONTACTS / CHAT / MAIL general
        if (path.startsWith("/api/v1/user/")) {
            return new RateLimitRule(10, 60, "user", "general");
        }
        if (path.startsWith("/api/v1/contacts/")) {
            return new RateLimitRule(10, 60, "contacts", "general");
        }
        if (path.startsWith("/api/v1/chat/")) {
            return new RateLimitRule(10, 60, "chat", "general");
        }
        if (path.startsWith("/api/v1/mail/")) {
            return new RateLimitRule(10, 60, "mail", "general");
        }

        return new RateLimitRule(30, 60, "general", "general");
    }

    private String getClientIp(ServerHttpRequest request) {
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"};

        for (String header : headers) {
            String ip = request.getHeaders().getFirst(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }

    @Data
    @AllArgsConstructor
    private static class RateLimitRule {
        private int maxRequests;         // capacity (burst)
        private int timeWindowSeconds;   // full refill window (seconds)
        private String scope;            // auth/mail/user...
        private String keySuffix;        // check_otp/reset_password/general...
    }
}

