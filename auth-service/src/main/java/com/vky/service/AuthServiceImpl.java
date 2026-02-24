package com.vky.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.vky.dto.request.*;
import com.vky.dto.response.*;
import com.vky.exception.AuthManagerException;
import com.vky.exception.ErrorType;
import com.vky.manager.IMailManager;
import com.vky.manager.IUserManager;
import com.vky.mapper.IAuthMapper;
import com.vky.rabbitmq.model.CreateUser;
import com.vky.rabbitmq.producer.RabbitMQProducer;
import com.vky.repository.IAuthRepository;
import com.vky.repository.entity.Auth;
import com.vky.repository.entity.enums.Role;
import com.vky.util.JwtTokenManager;
import feign.FeignException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements IAuthService {

    private final IAuthRepository authRepository;
    private final JwtTokenManager jwtTokenManager;
    private final PasswordEncoder passwordEncoder;
    private final RabbitMQProducer rabbitMQProducer;
    private final IMailManager mailManager;
    private final IUserManager iUserManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ReCaptchaService reCaptchaService;

    public AuthServiceImpl(IAuthRepository authRepository, JwtTokenManager jwtTokenManager,
            PasswordEncoder passwordEncoder,
            RabbitMQProducer rabbitMQProducer, IMailManager mailManager, IUserManager iUserManager,
            RedisTemplate<String, Object> redisTemplate, ReCaptchaService reCaptchaService) {
        this.authRepository = authRepository;
        this.jwtTokenManager = jwtTokenManager;
        this.passwordEncoder = passwordEncoder;
        this.rabbitMQProducer = rabbitMQProducer;
        this.mailManager = mailManager;
        this.iUserManager = iUserManager;
        this.redisTemplate = redisTemplate;
        this.reCaptchaService = reCaptchaService;
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        captcha(loginRequestDTO.recaptchaToken(), "login");

        Auth authUser = authRepository.findByEmailIgnoreCase(loginRequestDTO.email())
                .orElseThrow(() -> new AuthManagerException(ErrorType.INVALID_CREDENTIALS));

        if (!authUser.isApproved()) {
            throw new AuthManagerException(ErrorType.EMAIL_NEEDS_VERIFICATION);
        }

        if (!passwordEncoder.matches(loginRequestDTO.password(), authUser.getPassword())) {
            throw new AuthManagerException(ErrorType.INVALID_CREDENTIALS);
        }

        String token = jwtTokenManager.generateToken(authUser.getEmail(), authUser.getId());
        String refreshToken = jwtTokenManager.generateRefreshToken(authUser.getEmail(), authUser.getId());

        return LoginResponseDTO.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .id(authUser.getId())
                .build();
    }

    @Override
    public LoginResponseDTO refreshAuthenticationToken(String refreshToken) {
        try {
            DecodedJWT decoded = JWT.decode(refreshToken);
            UUID authId = UUID.fromString(decoded.getClaim("id").asString());

            String redisKey = "refreshToken:" + authId;
            String storedRefresh = (String) redisTemplate.opsForValue().get(redisKey);

            if (storedRefresh == null || !storedRefresh.equals(refreshToken)) {
                throw new AuthManagerException(ErrorType.INVALID_TOKEN);
            }

            jwtTokenManager.validateAndGet(refreshToken);

            String email = decoded.getSubject();

            Auth authUser = authRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new AuthManagerException(ErrorType.USER_NOT_FOUND));

            String newAccessToken = jwtTokenManager.generateToken(authUser.getEmail(), authUser.getId());
            String newRefreshToken = jwtTokenManager.generateRefreshToken(authUser.getEmail(), authUser.getId());

            return LoginResponseDTO.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .id(authUser.getId())
                    .build();

        } catch (TokenExpiredException e) {
            throw new AuthManagerException(ErrorType.TOKEN_EXPIRED);
        } catch (JWTVerificationException e) {
            throw new AuthManagerException(ErrorType.INVALID_TOKEN);
        }
    }

    @Override
    @Transactional
    public void register(RegisterRequestDTO registerRequestDTO) {
        captcha(registerRequestDTO.recaptchaToken(), "signup");

        Optional<Auth> optionalAuth = authRepository.findAuthByAndEmailIgnoreCase(registerRequestDTO.email());

        if (optionalAuth.isPresent()) {
            Auth existingAuth = optionalAuth.get();
            if (existingAuth.isApproved()) {
                throw new AuthManagerException(ErrorType.EMAIL_ALREADY_EXISTS);
            } else {
                existingAuth.setPassword(passwordEncoder.encode(registerRequestDTO.password()));
                authRepository.save(existingAuth);
                iUserManager.resetUserKey(ResetUserKeyDTO.builder().publicKey(registerRequestDTO.publicKey())
                        .salt(registerRequestDTO.salt()).encryptedPrivateKey(registerRequestDTO.encryptedPrivateKey())
                        .iv(registerRequestDTO.iv()).userId(existingAuth.getId()).build());
                ResendConfirmationRequestDTO confirmationDTO = new ResendConfirmationRequestDTO(
                        existingAuth.getEmail());
                mailManager.resendConfirmation(confirmationDTO);
            }
        } else {
            Auth registerAuth = createNewAuth(registerRequestDTO);
            sendConfirmationAndUserCreationMessages(registerAuth, registerRequestDTO);
        }
    }

    private void captcha(String recaptchaToken, String action) {
        ReCaptchaResponseDTO captchaResponse = reCaptchaService.verify(recaptchaToken);

        if (captchaResponse == null || !captchaResponse.success()) {
            throw new AuthManagerException(ErrorType.RECAPTCHA_FAILED);
        }

        if (!action.equals(captchaResponse.action())) {
            throw new AuthManagerException(ErrorType.RECAPTCHA_FAILED);
        }

        if (captchaResponse.score() < 0.5) {
            throw new AuthManagerException(ErrorType.RECAPTCHA_FAILED);
        }
    }

    private Auth createNewAuth(RegisterRequestDTO registerRequestDTO) {
        Auth auth = Auth.builder()
                .email(registerRequestDTO.email())
                .password(passwordEncoder.encode(registerRequestDTO.password()))
                .role(Role.USER)
                .isApproved(false)
                .build();
        return authRepository.save(auth);
    }

    private void sendConfirmationAndUserCreationMessages(Auth auth, RegisterRequestDTO registerRequestDTO) {
        CreateConfirmationRequestDTO confirmationDTO = IAuthMapper.INSTANCE.toAuthDTOO(auth);
        mailManager.createConfirmation(confirmationDTO);
        rabbitMQProducer.sendCreateUserMessage(CreateUser.builder()
                .authId(auth.getId())
                .email(auth.getEmail())
                .encryptedPrivateKey(registerRequestDTO.encryptedPrivateKey())
                .iv(registerRequestDTO.iv())
                .salt(registerRequestDTO.salt())
                .publicKey(registerRequestDTO.publicKey())
                .build());

    }

    @Override
    public boolean saveVerifiedAccountId(UUID id) {
        Auth existingAuth = authRepository.findById(id).orElse(null);
        if (existingAuth != null) {
            existingAuth.setApproved(true);
            this.authRepository.save(existingAuth);
            return true;
        } else {
            return false;
        }

    }

    @Override
    public ForgotPasswordResponseDTO createForgotPassword(String email) {
        try {
            Auth auth = this.authRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new AuthManagerException(ErrorType.EMAIL_NOT_FOUND));

            mailManager.createForgotPassword(ForgotPasswordRequestDTO.builder()
                    .email(auth.getEmail())
                    .authId(auth.getId())
                    .build());

            Long expirySeconds = redisTemplate.getExpire("reset_password:", TimeUnit.SECONDS);
            if (expirySeconds == null || expirySeconds <= 0) {
                expirySeconds = 180L;
            }
            Instant expiryTime = Instant.now().plusSeconds(expirySeconds);

            return ForgotPasswordResponseDTO.builder().email(auth.getEmail()).expiryTime(expiryTime).build();
        } catch (AuthManagerException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthManagerException(ErrorType.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public CheckOtpResponseDTO checkOtp(CheckOtpRequestDTO checkOtpRequestDTO) {

        Auth auth = this.authRepository.findByEmailIgnoreCase(checkOtpRequestDTO.email())
                .orElseThrow(() -> new AuthManagerException(ErrorType.EMAIL_NOT_FOUND));

        String redisKey = "reset_password:" + auth.getId();

        Map<Object, Object> resetData = redisTemplate.opsForHash().entries(redisKey);
        if (resetData.isEmpty()) {
            throw new AuthManagerException(ErrorType.PASSWORD_RESET_FAILED);
        }

        String storedOtp = (String) resetData.get("otp");
        String storedEmail = (String) resetData.get("email");
        int attempts = Integer.parseInt(resetData.get("attempts").toString());
        int attemptsLimit = Integer.valueOf(resetData.get("attempts_limit").toString());
        if (!storedEmail.equals(checkOtpRequestDTO.email())) {
            throw new AuthManagerException(ErrorType.EMAIL_MISMATCH);
        }

        if (attempts >= attemptsLimit) {
            redisTemplate.delete(redisKey);
            throw new AuthManagerException(ErrorType.TOO_MANY_ATTEMPTS);
        }

        if (storedOtp != null && storedOtp.equals(checkOtpRequestDTO.otp())) {
            String passwordResetToken = generateSecureToken();

            resetData.put("reset_token", passwordResetToken);
            resetData.put("otp_verified", "true");
            resetData.put("verified_at", Instant.now().toString());

            redisTemplate.opsForHash().putAll(redisKey, resetData);
            redisTemplate.expire(redisKey, Duration.ofMinutes(5));
            Long expirySeconds = redisTemplate.getExpire("reset_password:" + auth.getId(), TimeUnit.SECONDS);
            Instant expiryTime = Instant.now().plusSeconds(expirySeconds);

            return CheckOtpResponseDTO.builder().resetToken(passwordResetToken).email(auth.getEmail())
                    .expiryTime(expiryTime).success(true).build();
        } else {
            redisTemplate.opsForHash().put(redisKey, "attempts", String.valueOf(attempts + 1));

            if (attempts + 1 >= 3) {
                redisTemplate.delete(redisKey);
            }
            int remainingAttempts = attemptsLimit - (attempts + 1);
            return CheckOtpResponseDTO.builder().email(auth.getEmail()).remainingAttempts(remainingAttempts)
                    .success(false).message("Invalid OTP Code. Remaining attempts: " + remainingAttempts).build();
        }
    }

    @Override
    @Transactional
    public void resetPassword(ForgotPasswordResetPasswordRequestDTO forgotPasswordResetPasswordRequestDTO) {
        captcha(forgotPasswordResetPasswordRequestDTO.recaptchaToken(), "password_reset");
        Auth auth = this.authRepository.findAuthByAndEmailIgnoreCase(forgotPasswordResetPasswordRequestDTO.email())
                .orElseThrow(() -> new AuthManagerException(ErrorType.EMAIL_NOT_FOUND));

        String redisKey = "reset_password:" + auth.getId();

        Map<Object, Object> resetData = redisTemplate.opsForHash().entries(redisKey);

        if (resetData.isEmpty()) {
            throw new AuthManagerException(ErrorType.PASSWORD_RESET_FAILED);
        }
        String storedEmail = (String) resetData.get("email");
        String resetToken = (String) resetData.get("reset_token");
        String otpVerified = (String) resetData.get("otp_verified");

        if (!storedEmail.equals(forgotPasswordResetPasswordRequestDTO.email())) {
            throw new AuthManagerException(ErrorType.EMAIL_MISMATCH);
        }

        if (!resetToken.equals(forgotPasswordResetPasswordRequestDTO.resetToken())) {
            throw new AuthManagerException(ErrorType.INVALID_RESET_TOKEN);
        }

        if (!"true".equals(otpVerified)) {
            throw new AuthManagerException(ErrorType.OTP_NOT_VERIFIED);
        }
        String newPassword = this.passwordEncoder.encode(forgotPasswordResetPasswordRequestDTO.newPassword());

        auth.setPassword(newPassword);
        this.authRepository.save(auth);

        try {

            iUserManager.resetUserKey(ResetUserKeyDTO.builder()
                    .userId(auth.getId())
                    .iv(forgotPasswordResetPasswordRequestDTO.iv())
                    .encryptedPrivateKey(forgotPasswordResetPasswordRequestDTO.encryptedPrivateKey())
                    .salt(forgotPasswordResetPasswordRequestDTO.salt())
                    .publicKey(forgotPasswordResetPasswordRequestDTO.publicKey())
                    .build());
        } catch (FeignException e) {
            throw new AuthManagerException(ErrorType.USER_KEY_RESET_FAILED);
        }
        redisTemplate.delete(redisKey);
    }

    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Override
    public void changePassword(ChangePasswordRequestDTO changePasswordRequestDTO, String userId) {
        Auth auth = this.authRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new AuthManagerException(ErrorType.EMAIL_NOT_FOUND));
        String newPassword = this.passwordEncoder.encode(changePasswordRequestDTO.newPassword());
        auth.setPassword(newPassword);
        this.authRepository.save(auth);
        iUserManager.resetUserKey(ResetUserKeyDTO.builder()
                .userId(auth.getId())
                .iv(changePasswordRequestDTO.iv())
                .encryptedPrivateKey(changePasswordRequestDTO.encryptedPrivateKey())
                .salt(changePasswordRequestDTO.salt())
                .build());
    }
}
