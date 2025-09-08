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

    private final RedisTemplate<String, Object> redisTemplate;

    public JwtTokenManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generateToken(
            String email,
             UUID authId
    ) {
        return buildToken(email,  authId, jwtExpiration);
    }

    public String generateRefreshToken(String email,
             UUID authId
    ) {

        String refreshToken = buildToken(email,  authId , refreshExpiration);
        redisTemplate.opsForValue().set(
                "refreshToken:" + authId,
                refreshToken,
                refreshExpiration,
                TimeUnit.MILLISECONDS
        );
        return refreshToken;

    }


    public String buildToken(String email,
                              UUID authId,
                             long expiration) {
        Algorithm signKey = Algorithm.HMAC256(secretKey);
        return JWT.create()
                .withSubject(email)
                .withClaim("id", authId.toString())
                .withIssuedAt(new Date(System.currentTimeMillis()))
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .sign(signKey);
    }

    public String extractUsername(String token) {
        return extractClaim(token, DecodedJWT::getSubject);
    }

    public <T> T extractClaim(String token, Function<DecodedJWT, T> claimsResolver) {
        DecodedJWT decodedJWT = JWT.decode(token);
        return claimsResolver.apply(decodedJWT);
    }

    public boolean isValidToken(String token) {
        try {
            Algorithm signKey = Algorithm.HMAC256(secretKey);
            JWTVerifier verifier = JWT.require(signKey).build();
            verifier.verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, DecodedJWT::getExpiresAt);
    }
    public UUID extractAuthId(String token) {
        return UUID.fromString(extractClaim(token, decodedJWT -> decodedJWT.getClaim("id").asString()));
    }
    public String extractUsernameSecurely(String token) {
        try {
            Algorithm signKey = Algorithm.HMAC256(secretKey);
            JWTVerifier verifier = JWT.require(signKey).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT.getSubject();
        } catch (JWTVerificationException e) {
            throw e;
        }
    }

    public long getRemainingTTL(String token) {
        Date expirationDate = extractExpiration(token);
        long nowMillis = System.currentTimeMillis();
        long diffMillis = expirationDate.getTime() - nowMillis;
        return Math.max(diffMillis / 1000, 0);
    }
}
