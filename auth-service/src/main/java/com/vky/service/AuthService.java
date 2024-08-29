package com.vky.service;

import com.vky.config.security.JwtTokenManager;
import com.vky.dto.request.*;
import com.vky.dto.response.AuthResponseDTO;
import com.vky.dto.response.HttpResponse;
import com.vky.entity.Auth;
import com.vky.entity.enums.Role;
import com.vky.exception.AuthManagerException;
import com.vky.exception.ErrorType;
import com.vky.manager.IMailManager;
import com.vky.manager.IUserManager;
import com.vky.mapper.IAuthMapper;
import com.vky.rabbitmq.model.CreateUser;
import com.vky.rabbitmq.producer.RabbitMQProducer;
import com.vky.repository.IAuthRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    private final IUserManager userManager;
    private final IMailManager mailManager;

//    private final ObjectMapper objectMapper;


    public AuthService(IAuthRepository authRepository, JwtTokenManager jwtTokenManager, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, TokenService tokenService, IUserManager userManager, RabbitMQProducer rabbitMQProducer, IMailManager mailManager) {
        this.authRepository = authRepository;
        this.jwtTokenManager = jwtTokenManager;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.rabbitMQProducer = rabbitMQProducer;
        this.userManager = userManager;
        this.mailManager = mailManager;
//        this.objectMapper = objectMapper;
    }

//    public record LoginRequestDto (@Size(min = 3, max = 20, message = "Kullanici adi en az 3 en fazla 20 karakter icerebilir")String userName, String password){
//
//    }


    public AuthResponseDTO doLoginn(AuthRequestDTO authRequestDTO) {
        try {
            Optional<Auth> user = authRepository.findAuthByAndEmailIgnoreCase(authRequestDTO.getEmail());
            if (user.isEmpty()) {
                throw new AuthManagerException(ErrorType.USER_DOES_NOT_EXIST);
            } else if (!user.get().isApproved()) {
                throw new AuthManagerException(ErrorType.Email_Confirmation_Not_Completed);
            }
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(authRequestDTO.getEmail(), authRequestDTO.getPassword());
            Authentication auth = authenticationManager.authenticate(authToken);


            SecurityContextHolder.getContext().setAuthentication(auth);
            String jwtToken = jwtTokenManager.generateToken(auth,user.get().getId());


            AuthResponseDTO authResponseDTO = IAuthMapper.INSTANCE.toResponseDTO(user.get());
            authResponseDTO.setAccessToken("Bearer " + jwtToken);
            authResponseDTO.setRefreshToken(jwtTokenManager.generateRefreshToken(auth, user.get().getId()));
            authResponseDTO.setResponsecode(200L);
            this.tokenService.saveToken(user.get(),jwtToken);
            return authResponseDTO;
        } catch (BadCredentialsException ex) {
            AuthResponseDTO authResponseDTO = new AuthResponseDTO();
            authResponseDTO.setResponsecode(400L);
            authResponseDTO.setMessage("Kullanıcı adı veya şifre yanlış");
            return authResponseDTO;
        }
    }



    public AuthResponseDTO register(AuthRequestDTO authRequestDTO) {
        Optional<Auth> optionalAuth = authRepository.findAuthByAndEmailIgnoreCase(authRequestDTO.getEmail());

        if (optionalAuth.isPresent() && optionalAuth.get().isApproved())
            throw new AuthManagerException(ErrorType.EMAIL_ALLREADY_EXISTS_ERROR);
        else if (optionalAuth.isPresent() && !optionalAuth.get().isApproved()) {
            CreateConfirmationRequestDTO createConfirmationRequestDTO = IAuthMapper.INSTANCE.toAuthDTOO(optionalAuth.get());
            mailManager.createConfirmation(createConfirmationRequestDTO);
            throw new AuthManagerException(ErrorType.EMAIL_ALLREADY_EXISTS_ERROR_VERIFIY);
        }

        Auth registerAuth = Auth.builder()
                .email(authRequestDTO.getEmail())
                .password(passwordEncoder.encode(authRequestDTO.getPassword()))
                .role(Role.USER)
                .build();
        this.authRepository.save(registerAuth);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(authRequestDTO.getEmail(), authRequestDTO.getPassword());
        Authentication auth = authenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(auth);
        String jwt = jwtTokenManager.generateToken(auth, registerAuth.getId());
        this.tokenService.saveToken(registerAuth, jwt);

        CreateConfirmationRequestDTO createConfirmationRequestDTO = IAuthMapper.INSTANCE.toAuthDTOO(registerAuth);
        mailManager.createConfirmation(createConfirmationRequestDTO);
        rabbitMQProducer.sendCreateUserMessage(CreateUser.builder()
                .authId(registerAuth.getId())
                .email(registerAuth.getEmail())
                .build());
        return AuthResponseDTO.builder()
                .accessToken("Bearer " + jwt)
                .refreshToken(jwtTokenManager.generateRefreshToken(auth, registerAuth.getId()))
                .message("Kayit İşlemi Başarılı")
                .responsecode(200L)
                .id(registerAuth.getId())
                .build();
    }


    public Auth findById(UUID id) {
        return this.authRepository.findById(id).orElseThrow();
    }


    public Auth loadUserByUsername(String email) {
        return this.authRepository.findAuthByAndEmailIgnoreCase(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
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
