package com.vky.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vky.exception.AuthManagerException;
import com.vky.exception.ErrorType;
import com.vky.repository.IAuthRepository;
import com.vky.repository.entity.Auth;
import feign.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ConfigurationSpring {
    private final IAuthRepository authRepository;
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            Auth auth = authRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new BadCredentialsException("Invalid Credentials"));
            if (!auth.isApproved()) {
                throw new AuthManagerException(ErrorType.EMAIL_NEEDS_VERIFICATION);
            }
            return auth;
        };

    }
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL; // Veya DEBUG veya başka bir seviye
    }
}
