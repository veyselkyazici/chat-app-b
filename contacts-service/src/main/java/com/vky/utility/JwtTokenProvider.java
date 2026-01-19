package com.vky.utility;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtTokenProvider {
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;
    @Value("${application.security.jwt.issuer}")
    private String issuer;

    private Algorithm algorithm() {
        return Algorithm.HMAC256(secretKey);
    }

    private JWTVerifier verifier() {
        return JWT
                .require(algorithm())
                .withIssuer(issuer)
                .build();
    }


    public DecodedJWT validateAndGet(String token) {
        return verifier().verify(token);
    }

    public boolean isValidToken(String token) {
        try {
            validateAndGet(token);
            return true;
        } catch (TokenExpiredException e) {
            throw e;
        } catch (JWTVerificationException e) {
            return false;
        }
    }


    public UUID extractAuthId(String token) {
        DecodedJWT jwt = validateAndGet(token);
        return UUID.fromString(jwt.getClaim("id").asString());
    }

    public <T> T extractClaim(String token, Function<DecodedJWT, T> resolver) {
        DecodedJWT jwt = validateAndGet(token);
        return resolver.apply(jwt);
    }
}
