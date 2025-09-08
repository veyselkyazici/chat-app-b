package com.vky.utility;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtTokenProvider {
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;


    public <T> T extractClaim(String token, Function<DecodedJWT, T> claimsResolver) {
        DecodedJWT decodedJWT = JWT.decode(token);
        return claimsResolver.apply(decodedJWT);
    }

    public boolean isValidToken(String token) {
        try {
            Algorithm signKey = Algorithm.HMAC256(secretKey);
            JWTVerifier verifier = JWT.require(signKey)
                    .build();
            verifier.verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public UUID extractAuthId(String token) {
        return UUID.fromString(extractClaim(token, decodedJWT -> decodedJWT.getClaim("id").asString()));
    }
}
