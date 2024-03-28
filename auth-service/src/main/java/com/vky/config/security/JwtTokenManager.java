package com.vky.config.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtTokenManager {
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;
    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;


    public String generateToken(Authentication auth, UUID authId) {
        return generateToken(new HashMap<>(), auth, authId);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            Authentication auth, UUID authId
    ) {
        return buildToken(extraClaims, auth, authId, jwtExpiration);
    }

    public String generateRefreshToken(
            Authentication auth, UUID authId
    ) {
        return buildToken(new HashMap<>(), auth, authId , refreshExpiration);
    }


    public String buildToken(Map<String, Object> extraClaims,
                             Authentication auth, UUID authId,
                             long expiration) {
        Algorithm signKey = Algorithm.HMAC256(secretKey);
        String jwt = JWT.create()
                .withSubject(auth.getName())
                .withClaim("authId", authId.toString())
                .withIssuedAt(new Date(System.currentTimeMillis()))
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .sign(signKey);
        return jwt;
    }

    public String extractUsername(String token) {
        return extractClaim(token, DecodedJWT::getSubject);
    }

    public <T> T extractClaim(String token, Function<DecodedJWT, T> claimsResolver) {
        DecodedJWT decodedJWT = JWT.decode(token);
        return claimsResolver.apply(decodedJWT);
    }

    public boolean isValidToken(String token) {
        return isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).after(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, DecodedJWT::getExpiresAt);
    }





}
