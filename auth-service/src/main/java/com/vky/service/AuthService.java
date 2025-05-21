package com.vky.service;

import com.vky.config.security.JwtTokenManager;
import com.vky.dto.request.*;
import com.vky.dto.response.HttpResponse;
import com.vky.dto.response.LoginResponseDTO;
import com.vky.exception.AuthManagerException;
import com.vky.exception.ErrorType;
import com.vky.manager.IMailManager;
import com.vky.mapper.IAuthMapper;
import com.vky.rabbitmq.model.CreateUser;
import com.vky.rabbitmq.producer.RabbitMQProducer;
import com.vky.repository.IAuthRepository;
import com.vky.repository.entity.Auth;
import com.vky.repository.entity.enums.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    private final IAuthRepository authRepository;
    private final JwtTokenManager jwtTokenManager;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RabbitMQProducer rabbitMQProducer;
    private final IMailManager mailManager;


    public AuthService(IAuthRepository authRepository, JwtTokenManager jwtTokenManager, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, TokenService tokenService,RabbitMQProducer rabbitMQProducer, IMailManager mailManager) {
        this.authRepository = authRepository;
        this.jwtTokenManager = jwtTokenManager;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.rabbitMQProducer = rabbitMQProducer;
        this.mailManager = mailManager;
    }



    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        Auth authUser = authRepository.findByEmailIgnoreCase(loginRequestDTO.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!authUser.isApproved()) {
            throw new AuthManagerException(ErrorType.EMAIL_NEEDS_VERIFICATION);
        }

        try {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword());
            Authentication auth = authenticationManager.authenticate(authToken);


            String jwtToken = jwtTokenManager.generateToken(auth, authUser.getId());

            LoginResponseDTO loginResponseDTO = LoginResponseDTO.builder()
                    .accessToken(jwtToken)
                    .refreshToken(jwtTokenManager.generateRefreshToken(auth, authUser.getId()))
                    .id(authUser.getId())
                    .build();
            loginResponseDTO.setAccessToken(formatToken(jwtToken));
            loginResponseDTO.setRefreshToken(formatToken(jwtTokenManager.generateRefreshToken(auth, authUser.getId())));

            tokenService.saveToken(authUser, jwtToken);

            return loginResponseDTO;
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    private String formatToken(String token) {
        return "Bearer " + token;
    }
    @Transactional
    public void register(RegisterRequestDTO registerRequestDTO) {
        Optional<Auth> optionalAuth = authRepository.findAuthByAndEmailIgnoreCase(registerRequestDTO.getEmail());

        if (optionalAuth.isPresent()) {
            if (optionalAuth.get().isApproved()) {
                throw new AuthManagerException(ErrorType.EMAIL_ALREADY_EXISTS);
            } else {
                CreateConfirmationRequestDTO createConfirmationRequestDTO = IAuthMapper.INSTANCE.toAuthDTOO(optionalAuth.get());
                mailManager.createConfirmation(createConfirmationRequestDTO);
                throw new AuthManagerException(ErrorType.EMAIL_NEEDS_VERIFICATION);
            }
        }

        Auth registerAuth = createNewAuth(registerRequestDTO);
        sendConfirmationAndUserCreationMessages(registerAuth, registerRequestDTO);
    }

    private Auth createNewAuth(RegisterRequestDTO registerRequestDTO) {
        Auth auth = Auth.builder()
                .email(registerRequestDTO.getEmail())
                .password(passwordEncoder.encode(registerRequestDTO.getPassword()))
                .role(Role.USER)
                .build();
        return authRepository.save(auth);
    }

    private void sendConfirmationAndUserCreationMessages(Auth auth, RegisterRequestDTO registerRequestDTO) {
        CreateConfirmationRequestDTO confirmationDTO = IAuthMapper.INSTANCE.toAuthDTOO(auth);
        mailManager.createConfirmation(confirmationDTO);

        rabbitMQProducer.sendCreateUserMessage(CreateUser.builder()
                .authId(auth.getId())
                .email(auth.getEmail())
                .encryptedPrivateKey(registerRequestDTO.getEncryptedPrivateKey())
                .iv(registerRequestDTO.getIv())
                .salt(registerRequestDTO.getSalt())
                .publicKey(registerRequestDTO.getPublicKey())
                .build());
    }

    public Optional<Auth> loadUserByUsername(String email) {
        return this.authRepository.findAuthByAndEmailIgnoreCase(email);
    }

    public void saveVerifiedAccount(UUID id) {
        Auth existingAuth = authRepository.findById(id).orElse(null);
        if (existingAuth != null) {
            existingAuth.setApproved(true);
            this.authRepository.save(existingAuth);
        } else {
        }

    }

    public Optional<Auth> createForgotPassword(String email) {
        Optional<Auth> auth = this.authRepository.findByEmailIgnoreCase(email);
        if (auth.isEmpty()) {
            throw new AuthManagerException(ErrorType.EMAIL_NOT_FOUND);
        }
        mailManager.createForgotPassword(ForgotPasswordRequestDTO.builder()
                .password(auth.get().getPassword())
                .email(auth.get().getEmail())
                .authId(auth.get().getId())
                .build());
        return auth;

    }

    public HttpResponse findByEmailOtp(CheckOtpRequestDTO checkOtpRequestDTO) {
        Optional<Auth> auth = this.authRepository.findAuthByAndEmailIgnoreCase(checkOtpRequestDTO.getEmail());
        if (auth.isPresent()) {
            ForgotPasswordCheckOtpRequestDTO forgotPasswordCheckOtpRequestDTO = ForgotPasswordCheckOtpRequestDTO.builder()
                    .email(auth.get().getEmail())
                    .authId(auth.get().getId())
                    .otp(checkOtpRequestDTO.getOtp()).build();
            ResponseEntity<HttpResponse> response = mailManager.checkOtp(forgotPasswordCheckOtpRequestDTO);
            HttpResponse responseBody = response.getBody();
            return responseBody;
        }
        return null;
    }

    public void resetPassword(ForgotPasswordResetPasswordRequestDTO forgotPasswordResetPasswordRequestDTO) {
        String newPassword = this.passwordEncoder.encode(forgotPasswordResetPasswordRequestDTO.getNewPassword());
        forgotPasswordResetPasswordRequestDTO.setNewPassword(newPassword);
        ResponseEntity<HttpResponse> response = this.mailManager.resetPassword(forgotPasswordResetPasswordRequestDTO);
        String authId = response.getBody().getData().get("authId").toString();
        UUID authUUID = UUID.fromString(authId);
        Optional<Auth> auth = authRepository.findById(authUUID);

        if (auth.isPresent()) {
            auth.get().setPassword(newPassword);
            this.authRepository.save(auth.get());
        }
    }
}
