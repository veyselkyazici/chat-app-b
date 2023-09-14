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
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public String generateRefreshToken(
            UserDetails userDetails
    ) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }


    public String buildToken(Map<String, Object> extraClaims,
                             UserDetails userDetails,
                             long expiration) {
        Algorithm signKey = Algorithm.HMAC256(secretKey);
        JWTCreator.Builder jwt = JWT.create()
                .withSubject(userDetails.getUsername())
                .withIssuedAt(new Date(System.currentTimeMillis()))
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration));
        return jwt.sign(signKey);
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
