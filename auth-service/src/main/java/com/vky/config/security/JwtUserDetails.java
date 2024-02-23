package com.vky.config.security;

import com.auth0.jwt.interfaces.Claim;
import com.vky.entity.Auth;
import com.vky.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class JwtUserDetails implements UserDetailsService {
    @Autowired
    AuthService authService;
    private JwtTokenManager jwtTokenManager;
    public JwtUserDetails (JwtTokenManager jwtTokenManager) {
        this.jwtTokenManager = jwtTokenManager;
    }



    public UserDetails loadUserByUserId(Map<String,Claim> claimMap) throws UsernameNotFoundException {

        /**
         * id si verilen kullanıcının var olup olmadığına bakılmalıdır.
         */
        UUID userId = UUID.fromString(claimMap.get("authId").asString());
        for (Map.Entry<String, Claim> entry : claimMap.entrySet()) {
            String key = entry.getKey();
            Claim value = entry.getValue();
            System.out.println("Key: " + key + ", Value: " + value);
        }
        Auth auth = authService.findById(userId);
        //boolean isUserExist = authService.findById(claimMap.get("id").asLong()).getId() != null;
        if(auth != null){
            /**
             * Burada oluşturulan kullanıcı, hangi sayfalara griş yapabileceğinin
             * anlaşılabilmedi konytorl edilebilmesi için bir yetki listesininin
             * olmasına gerek vardır. bu nedenle burada "USER", "ADMIN" v.s. gibi
             * listeyi burada belirtmeliyiz.
             */
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

            return User.builder()
                    .username(auth.getUsername())
                    .password(auth.getPassword())
                    .accountExpired(false)
                    .accountLocked(false)
                    .authorities(authorities)
                    .build();
        }
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserDetails userDetails = authService.loadUserByUsername(email);
        return userDetails;
    }
}
