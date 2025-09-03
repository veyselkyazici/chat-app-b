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
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;
    @Getter
    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;


    public <T> T extractClaim(String token, Function<DecodedJWT, T> claimsResolver) {
        DecodedJWT decodedJWT = JWT.decode(token);
        return claimsResolver.apply(decodedJWT);
    }
    private Date extractExpiration(String token) {
        return extractClaim(token, DecodedJWT::getExpiresAt);
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
    public UUID extractAuthId(String token) {
        return UUID.fromString(extractClaim(token, decodedJWT -> decodedJWT.getClaim("id").asString()));
    }
    public long getRemainingTTL(String token) {
        Date expirationDate = extractExpiration(token);
        long nowMillis = System.currentTimeMillis();
        long diffMillis = expirationDate.getTime() - nowMillis;
        return Math.max(diffMillis / 1000, 0);
    }
}
