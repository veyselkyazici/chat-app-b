package com.vky.config.security;


import com.auth0.jwt.interfaces.Claim;
import com.vky.entity.Auth;
import com.vky.repository.ITokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;


public class JwtTokenFilter extends OncePerRequestFilter {
    @Autowired
    JwtTokenManager jwtTokenManager;
    @Autowired
    JwtUserDetails jwtUserDetails;
    @Autowired
    ITokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        /**
         * Gelen istegin header kisminin icinde Authorization anahtari var mi var ise icinde ne var bu bilgiyi aliyorum
         * bu bilgi icinde Bearer ile baslayan bir token bilgisi olabilidir.
         */

        final String authorizationHeader = request.getHeader("Authorization");
//        final String getBodyToken =  request.getParameter("Token");
        /**
         * Gelen string ve request icindeki oturum bilgisini kontrol ederek isleme devam ediyorum.
         */

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = authorizationHeader.substring(7);
        Map<String, Claim> claimMap = jwtTokenManager.getClaims(token);
        String email = jwtUserDetails.loadUserByUserId(claimMap).getUsername();
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.jwtUserDetails.loadUserByUsername(email);
            var isTokenValid = tokenRepository.findByToken(token)
                    .map(t -> !t.isExpired() && !t.isRevoked()).orElse(false);
            if (jwtTokenManager.isValidToken(token, userDetails) && isTokenValid) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        filterChain.doFilter(request, response);


//        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ") &&
//                SecurityContextHolder.getContext().getAuthentication() == null) {
//
//            if(!claimMap.isEmpty()) {
//            /**
//             * Spring icin gerekli olan oturum kullanicisinin tanimlanmasi gereklidir.
//             * bunu spring UserDetails sinifindan turetilmis ozellestirilmis bir kullanicinin
//             * olusturularak eklenmesi gereklidir.
//             */
//            UserDetails userDetails = jwtUserDetails.loadUserByUserId(claimMap);
//            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
//                    userDetails, null, userDetails.getAuthorities()
//            );
//            SecurityContextHolder.getContext().setAuthentication(authToken);
//        }
//    }
//        filterChain.doFilter(request,response);
    }

}
