package com.vky.config.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.impl.ClaimsHolder;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.vky.entity.Auth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Service
public class JwtTokenManager {
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;
    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;


    //    public Optional<String> createToken(Auth auth)
//    {
//        String token = null;
//        /**
//         * "wwltFQ5$|dM;`C8Gv#{=3m3e_T{7,0Ck]xN!wlYr4xA5CAW/V^YwkN2\"nX=rPJ8";
//         */
//        String sifreAnahtari = "1234";
//        try {
//            /**
//             * JWT icerisine hassas bilgilerinizi koymayiniz . Ornegin sifre gibi
//             */
//            token = JWT.create()
//                    .withAudience()
//                    .withClaim("id", auth.getId())
//                    .withClaim("username", auth.getUsername())
//                    .withClaim("password",encryptPassword(auth.getPassword()))
//                    .withIssuer("veysel")
//                    .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60))
//                    .withIssuedAt(new Date())
//                    .sign(Algorithm.HMAC256(sifreAnahtari));
//            return Optional.of(token);
//        }catch (Exception e)
//        {
//            return Optional.empty();
//        }
//    }
    public String generateToken(Authentication auth) {
        return generateToken(new HashMap<>(), auth);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            Authentication auth
    ) {
        return buildToken(extraClaims, auth, jwtExpiration);
    }

    public String generateRefreshToken(
            Authentication auth
    ) {
        return buildToken(new HashMap<>(), auth, refreshExpiration);
    }


    public String buildToken(Map<String, Object> extraClaims,
                             Authentication auth,
                             long expiration) {
        Algorithm signKey = Algorithm.HMAC256(secretKey);
        String jwt = JWT.create()
                .withSubject(auth.getName())
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

    public boolean isValidToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, DecodedJWT::getExpiresAt);
    }

    public Map<String, Claim> getClaims(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        JWTVerifier verifier = JWT.require(algorithm)
                .build();
        DecodedJWT decode = verifier.verify(token);
        return decode == null ? null : decode.getClaims();
    }


}
