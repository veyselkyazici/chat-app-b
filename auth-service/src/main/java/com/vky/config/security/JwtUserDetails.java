package com.vky.config.security;

import com.vky.repository.entity.Auth;
import com.vky.exception.AuthManagerException;
import com.vky.exception.ErrorType;
import com.vky.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
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
            throw new AuthManagerException(ErrorType.EMAIL_NEEDS_VERIFICATION);
        }
        return new org.springframework.security.core.userdetails.User(auth.getEmail(), auth.getPassword(), new ArrayList<>());
    }
}
