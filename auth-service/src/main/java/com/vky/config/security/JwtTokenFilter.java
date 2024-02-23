package com.vky.config.security;


import com.auth0.jwt.interfaces.Claim;
import com.vky.entity.Auth;
import com.vky.repository.ITokenRepository;
import com.vky.service.TokenService;
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
    private JwtTokenManager jwtTokenManager;
    @Autowired
    private JwtUserDetails jwtUserDetails;
    @Autowired
    private TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        /**
         * Gelen istegin header kisminin icinde Authorization anahtari var mi var ise icinde ne var bu bilgiyi aliyorum
         * bu bilgi icinde Bearer ile baslayan bir token bilgisi olabilidir.
         */
        System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
        final String authorizationHeader = request.getHeader("Authorization");
//        final String getBodyToken =  request.getParameter("Token");
        /**
         * Gelen string ve request icindeki oturum bilgisini kontrol ederek isleme devam ediyorum.
         */
        System.out.println("BEARER ONCESIIIIIIIIIIIIIIIIIIIIIIIII");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = authorizationHeader.substring(7);
        System.out.println("TOKENNNNNNNNN: " + token);
        Map<String, Claim> claimMap = jwtTokenManager.getClaims(token);
        claimMap.forEach( (key, value) -> System.out.println("CLAIMMMMMMMMMMMM: " + key + " -> " + value));
        String email = jwtUserDetails.loadUserByUserId(claimMap).getUsername();
        System.out.println("SECURITYCONTEXTHOLDER: " + SecurityContextHolder.getContext().getAuthentication());
        System.out.println("EMAILLLLLLLLLLLL: " + email);
        System.out.println("REMOTEADR: " + request.getRemoteAddr());
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.jwtUserDetails.loadUserByUsername(email);
            System.out.println("userDetails" + userDetails);
            System.out.println("userDetailsAuthorities" + userDetails.getAuthorities());
            System.out.println("userDetailsUsername" + userDetails.getUsername());
            System.out.println("userDetailsPassword" + userDetails.getPassword());

            Boolean isTokenValid = tokenService.findByToken(token);
            if (jwtTokenManager.isValidToken(token, userDetails) && isTokenValid) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                System.out.println("SECURITYCONTEXTHOLDER: " + SecurityContextHolder.getContext().getAuthentication());
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
