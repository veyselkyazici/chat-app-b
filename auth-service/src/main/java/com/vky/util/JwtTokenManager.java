package com.vky.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
public class JwtTokenManager {
    @Value("${services.security.jwt.secret-key}")
    private String secretKey;
    @Value("${services.security.jwt.expiration}")
    private long jwtExpiration;
    @Getter
    @Value("${services.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;
    @Value("${services.security.jwt.issuer}")
    private String issuer;

    private final RedisTemplate<String, Object> redisTemplate;

    public JwtTokenManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    private Algorithm algorithm() {
        return Algorithm.HMAC256(secretKey);
    }

    private JWTVerifier verifier() {
        return JWT.require(algorithm())
                .withIssuer(issuer)
                .build();
    }


    public DecodedJWT validateAndGet(String token) throws JWTVerificationException {
        return verifier().verify(token);
    }



    public String generateToken(String email, UUID authId) {
        String jti = UUID.randomUUID().toString();

        // ✅ aktif session'ın jti'siı redis'e yaz
        redisTemplate.opsForValue().set(
                "auth:active:" + authId,   // authId ile tut (öneri)
                jti,
                jwtExpiration,
                TimeUnit.MILLISECONDS
        );

        return buildAccessToken(email, authId, jwtExpiration, jti);
    }

    public String generateRefreshToken(String email, UUID authId) {
        String refreshToken = buildRefreshToken(email, authId, refreshExpiration);

        redisTemplate.opsForValue().set(
                "refreshToken:" + authId,
                refreshToken,
                refreshExpiration,
                TimeUnit.MILLISECONDS
        );

        return refreshToken;
    }

    private String buildAccessToken(String email, UUID authId, long expirationMs, String jti) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return JWT.create()
                .withSubject(email)
                .withClaim("id", authId.toString())
                .withJWTId(jti)                 // ✅ jti eklendi
                .withIssuer(issuer)
                .withIssuedAt(now)
                .withExpiresAt(expiry)
                .sign(algorithm());
    }

    private String buildRefreshToken(String email, UUID authId, long expirationMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return JWT.create()
                .withSubject(email)
                .withClaim("id", authId.toString())
                // refresh token'a jti koymak zorunda değilsin
                .withIssuer(issuer)
                .withIssuedAt(now)
                .withExpiresAt(expiry)
                .sign(algorithm());
    }


    public <T> T extractClaim(String token, Function<DecodedJWT, T> resolver) {
        DecodedJWT jwt = validateAndGet(token);
        return resolver.apply(jwt);
    }

    public String extractUsername(String token) {
        return extractClaim(token, DecodedJWT::getSubject);
    }

    public UUID extractAuthId(String token) {
        return extractClaim(token, jwt -> UUID.fromString(jwt.getClaim("id").asString()));
    }


    public boolean isValidToken(String token) {
        try {
            validateAndGet(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public long getRemainingTTL(String token) {
        DecodedJWT decoded = JWT.decode(token);
        Date expirationDate = decoded.getExpiresAt();

        long nowMillis = System.currentTimeMillis();
        long diffMillis = expirationDate.getTime() - nowMillis;

        return Math.max(diffMillis / 1000, 0);
    }
}
