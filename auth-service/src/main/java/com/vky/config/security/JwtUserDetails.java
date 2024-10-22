package com.vky.config.security;

import com.auth0.jwt.interfaces.Claim;
import com.vky.entity.Auth;
import com.vky.exception.AuthManagerException;
import com.vky.exception.ErrorType;
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

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Auth auth = authService.loadUserByUsername(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (!auth.isApproved()) {
            throw new AuthManagerException(ErrorType.Email_Confirmation_Not_Completed);
        }
        return new org.springframework.security.core.userdetails.User(auth.getEmail(), auth.getPassword(), new ArrayList<>());
    }
}
