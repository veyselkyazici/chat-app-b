package com.vky.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vky.config.security.JwtTokenManager;
import com.vky.dto.AdminGenerateRequestDTO;
import com.vky.dto.AdminGenerateResponseDTO;
import com.vky.dto.request.*;
import com.vky.dto.response.*;
import com.vky.entity.Auth;
import com.vky.entity.Token;
import com.vky.entity.enums.Role;
import com.vky.exception.AuthManagerException;
import com.vky.exception.ErrorType;
import com.vky.manager.IEmailVerifyManager;
import com.vky.manager.IForgotPasswordManager;
import com.vky.manager.IUserManager;
import com.vky.mapper.IAuthMapper;
import com.vky.repository.IAuthRepository;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    private final IAuthRepository authRepository;
    private final JwtTokenManager jwtTokenManager;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    //    private final CreateUserProducer createUserProducer;
    private final IUserManager userManager;
    private final IEmailVerifyManager emailVerifyManager;
    private final IForgotPasswordManager forgotPasswordManager;

//    private final ObjectMapper objectMapper;


    public AuthService(IAuthRepository authRepository, JwtTokenManager jwtTokenManager, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, TokenService tokenService, IUserManager userManager, IEmailVerifyManager emailVerifyManager, IForgotPasswordManager forgotPasswordManager) {
        this.authRepository = authRepository;
        this.jwtTokenManager = jwtTokenManager;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
//        this.createUserProducer = createUserProducer;
        this.userManager = userManager;
        this.emailVerifyManager = emailVerifyManager;
        this.forgotPasswordManager = forgotPasswordManager;
//        this.objectMapper = objectMapper;
    }

//    public record LoginRequestDto (@Size(min = 3, max = 20, message = "Kullanici adi en az 3 en fazla 20 karakter icerebilir")String userName, String password){
//
//    }


    public AuthLoginResponseDTO doLoginn(AuthLoginRequestDTO loginDto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getEmail(),
                        loginDto.getPassword()
                ));
        var auth = authRepository.findByEmail(loginDto.getEmail()).orElseThrow();
        var jwt = jwtTokenManager.generateToken(auth);
        var refreshToken = jwtTokenManager.generateRefreshToken(auth);
        this.tokenService.revokeAllAuthTokens(auth);
        this.tokenService.saveToken(auth, jwt);
        return AuthLoginResponseDTO.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken)
                .message("Giriş İşlemi Başarılı")
                .responsecode(200L)
                .build();
    }

//    public record RegisterRequestDto (String userName, String password, @Email String email){
//
//    }

    public AuthRegisterResponseDTO register(AuthRegisterRequestDTO authRegisterRequestDto) {
        Optional<Auth> auth = authRepository.findByEmail(authRegisterRequestDto.getEmail());
        Auth registerAuth = Auth.builder()
                .email(authRegisterRequestDto.getEmail())
                .password(passwordEncoder.encode(authRegisterRequestDto.getPassword()))
                .role(Role.USER)
                .build();

        if (auth.isPresent() && auth.get().isEnabled())
            throw new AuthManagerException(ErrorType.EMAIL_ALLREADY_EXISTS_ERROR);
        else if (auth.isPresent() && !auth.get().isEnabled()) {
            CreateConfirmationRequestDTO createConfirmationRequestDTO = IAuthMapper.INSTANCE.toAuthDTOO(auth.get());
            emailVerifyManager.createConfirmation(createConfirmationRequestDTO);
            throw new AuthManagerException(ErrorType.EMAIL_ALLREADY_EXISTS_ERROR_VERIFIY);
        }

        //auth.setId(UUID.randomUUID());
        this.authRepository.save(registerAuth);
        var jwt = jwtTokenManager.generateToken(registerAuth);
        var refreshToken = jwtTokenManager.generateRefreshToken(registerAuth);
        this.tokenService.saveToken(registerAuth, jwt);
//        User service e kullanicinin profilini olusturmasi icin istek gonderiri
        userManager.newUserCreate(
                NewUserCreateDTO.builder()
                        .authId(registerAuth.getId())
                        .email(authRegisterRequestDto.getEmail())
                        .build()
        );
//        SaveConfirmationResponseDTO.builder()
//                .email(auth.getEmail())
//                .id(auth.getId())
//                .build();
        CreateConfirmationRequestDTO createConfirmationRequestDTO = IAuthMapper.INSTANCE.toAuthDTOO(registerAuth);
        emailVerifyManager.createConfirmation(createConfirmationRequestDTO);
//        createUserProducer.sendCreateUserMessage(CreateUser.builder()
//                .authId(auth.getId())
//                .email(registerDto.getEmail())
//                .username(registerDto.getUsername())
//                .password(encodedPassword)
//                .build());

        return AuthRegisterResponseDTO.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken)
                .message("Kayit İşlemi Başarılı")
                .responsecode(200L)
                .build();
    }

    public AdminGenerateResponseDTO generateAdmin(AdminGenerateRequestDTO adminGenerateRequestDto) {
        Optional<Auth> admin = authRepository.findByEmail(adminGenerateRequestDto.getEmail());
        System.out.println("ADMIN: " + admin);
        if (!admin.isPresent()) {
            Auth auth = Auth.builder()
                    .email(adminGenerateRequestDto.getEmail())
                    .password(passwordEncoder.encode(adminGenerateRequestDto.getPassword()))
                    .role(adminGenerateRequestDto.getRole())
                    .build();
            authRepository.save(auth);
            var jwt = jwtTokenManager.generateToken(auth);
            var refreshToken = jwtTokenManager.generateRefreshToken(auth);
            System.out.println("UUID: " + auth.getId());
            this.tokenService.saveToken(auth, jwt);
            return AdminGenerateResponseDTO.builder()
                    .accessToken(jwt)
                    .build();
        }
        System.out.println("ADMIN ID: " + admin.get().getId());
        Optional<Token> adminAccessToken = this.tokenService.findByAuthId(admin.get().getId());
        return AdminGenerateResponseDTO.builder()
                .accessToken(adminAccessToken.get().getToken())
                .build();
    }

    public Auth findById(UUID id) {
        return this.authRepository.findById(id).orElseThrow();
    }


    public Auth loadUserByUsername(String email) {
        return this.authRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public void saveVerifiedAccount(UUID id) {
        Auth existingAuth = authRepository.findById(id).orElse(null);
        if (existingAuth != null) {
            existingAuth.setEnabled(true);
            this.authRepository.save(existingAuth);
        } else {
            System.out.println("hataaaaaaaaaaaaaaaaaaaaaaaaaaa");
        }

    }

    public Optional<Auth> createForgotPassword(String email) {
        Optional<Auth> auth = this.authRepository.findByEmail(email);
        if (auth.isEmpty()) {
            throw new AuthManagerException(ErrorType.EMAIL_NOT_FOUND);
        }
        forgotPasswordManager.createForgotPassword(ForgotPasswordRequestDTO.builder()
                .password(auth.get().getPassword())
                .email(auth.get().getEmail())
                .authId(auth.get().getId())
                .build());
        return auth;

    }

    public HttpResponse findByEmailOtp(CheckOtpRequestDTO checkOtpRequestDTO) {
        Optional<Auth> auth = this.authRepository.findByEmail(checkOtpRequestDTO.getEmail());
        if (auth.isPresent()) {
            ForgotPasswordCheckOtpRequestDTO forgotPasswordCheckOtpRequestDTO = ForgotPasswordCheckOtpRequestDTO.builder()
                    .email(auth.get().getEmail())
                    .authId(auth.get().getId())
                    .otp(checkOtpRequestDTO.getOtp()).build();
            ResponseEntity<HttpResponse> response = forgotPasswordManager.checkOtp(forgotPasswordCheckOtpRequestDTO);
            HttpResponse responseBody = response.getBody();
            System.out.println(response.getBody());
            System.out.println(response.getBody().toString());
            return responseBody;
        }
        return null;
    }

    public void resetPassword(ForgotPasswordResetPasswordRequestDTO forgotPasswordResetPasswordRequestDTO) {
        String newPassword = this.passwordEncoder.encode(forgotPasswordResetPasswordRequestDTO.getNewPassword());
        forgotPasswordResetPasswordRequestDTO.setNewPassword(newPassword);
        ResponseEntity<HttpResponse> response = this.forgotPasswordManager.resetPassword(forgotPasswordResetPasswordRequestDTO);
        System.out.println("RESPONSE: " + response);
        String authId = response.getBody().getData().get("authId").toString();
        UUID authUUID = UUID.fromString(authId);
        Optional<Auth> auth = authRepository.findById(authUUID);

        if(auth.isPresent()) {
            auth.get().setPassword(newPassword);
            this.authRepository.save(auth.get());
        }
    }
}
