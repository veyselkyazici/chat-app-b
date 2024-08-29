package com.vky.service;

import com.vky.entity.Auth;
import com.vky.entity.Token;
import com.vky.repository.ITokenRepository;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;
import java.util.UUID;

@Service
public class TokenService {
    private final ITokenRepository tokenRepository;

    public TokenService(ITokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public void saveToken(Auth auth, String jwtToken) {
        var token = Token.builder()
                .auth(auth)
                .token(jwtToken)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }
    public void revokeAllAuthTokens(Auth auth) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(auth.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public Optional<Token> findByAuthId(UUID id) {
        return this.tokenRepository.findByAuthId(id);
    }

    public Boolean findByToken(String token) {
        return tokenRepository.findByToken(token).map(t -> !t.isExpired() && !t.isRevoked()).orElse(false);
    }
}
