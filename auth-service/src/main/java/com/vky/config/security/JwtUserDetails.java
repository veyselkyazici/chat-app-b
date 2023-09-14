package com.vky.config.security;

import com.auth0.jwt.interfaces.Claim;
import com.vky.entity.Auth;
import com.vky.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        UUID userId = UUID.fromString(claimMap.get("id").asString());
        boolean isUserExist = authService.findById(userId) != null;
        //boolean isUserExist = authService.findById(claimMap.get("id").asLong()).getId() != null;
        if(isUserExist){
            /**
             * Burada oluşturulan kullanıcı, hangi sayfalara griş yapabileceğinin
             * anlaşılabilmedi konytorl edilebilmesi için bir yetki listesininin
             * olmasına gerek vardır. bu nedenle burada "USER", "ADMIN" v.s. gibi
             * listeyi burada belirtmeliyiz.
             */
            List<GrantedAuthority> authorities = new ArrayList<>();
            return User.builder()
                    .username(claimMap.get("id").asLong().toString())
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
