package com.vky.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.vky.util.JwtTokenManager;
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
public class AuthService {
    private final IAuthRepository authRepository;
    private final JwtTokenManager jwtTokenManager;
    private final PasswordEncoder passwordEncoder;
    private final RabbitMQProducer rabbitMQProducer;
    private final IMailManager mailManager;
    private final IUserManager iUserManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ReCaptchaService reCaptchaService;


    public AuthService(IAuthRepository authRepository, JwtTokenManager jwtTokenManager, PasswordEncoder passwordEncoder,  RabbitMQProducer rabbitMQProducer, IMailManager mailManager, IUserManager iUserManager, RedisTemplate<String, Object> redisTemplate, ReCaptchaService reCaptchaService) {
        this.authRepository = authRepository;
        this.jwtTokenManager = jwtTokenManager;
        this.passwordEncoder = passwordEncoder;
        this.rabbitMQProducer = rabbitMQProducer;
        this.mailManager = mailManager;
        this.iUserManager = iUserManager;
        this.redisTemplate = redisTemplate;
        this.reCaptchaService = reCaptchaService;
    }
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        captcha(loginRequestDTO.getRecaptchaToken(),"login");

        Auth authUser = authRepository.findByEmailIgnoreCase(loginRequestDTO.getEmail())
                .orElseThrow(() -> new AuthManagerException(ErrorType.INVALID_CREDENTIALS));

        if (!authUser.isApproved()) {
            throw new AuthManagerException(ErrorType.EMAIL_NEEDS_VERIFICATION);
        }

        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), authUser.getPassword())) {
            throw new AuthManagerException(ErrorType.INVALID_CREDENTIALS);
        }

        // ToDo email koy token claim içine
        String token = jwtTokenManager.generateToken(authUser.getEmail(), authUser.getId());
        String refreshToken = jwtTokenManager.generateRefreshToken(authUser.getEmail(), authUser.getId());

        return LoginResponseDTO.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .id(authUser.getId())
                .build();
    }

    public LoginResponseDTO refreshAuthenticationToken(String refreshToken) {
        try {
            // 1. Refresh token'ı doğrula ve email'i çıkar (expiry otomatik kontrol edilir)
            UUID authId = jwtTokenManager.extractAuthId(refreshToken);
            String email = jwtTokenManager.extractUsernameSecurely(refreshToken);

            // 2. Redis'teki refresh token ile karşılaştır
            String storedRefreshToken = (String) redisTemplate.opsForValue().get("refreshToken:" + authId);
            if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
                throw new AuthManagerException(ErrorType.INVALID_TOKEN);
            }

            // 3. Yeni token'lar oluştur (expiry kontrolü yukarıda yapıldı)
            Auth authUser = authRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new AuthManagerException(ErrorType.USER_NOT_FOUND));


            String newAccessToken = jwtTokenManager.generateToken(authUser.getEmail(), authUser.getId());
            String newRefreshToken = jwtTokenManager.generateRefreshToken(authUser.getEmail(), authUser.getId());

            // Eski refresh token'ı sil, yenisini kaydet
            redisTemplate.delete("refreshToken:" + authId);
            redisTemplate.opsForValue().set(
                    "refreshToken:" + authId,
                    newRefreshToken,
                    jwtTokenManager.getRefreshExpiration(),
                    TimeUnit.MILLISECONDS
            );

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
    @Transactional(rollbackFor=Exception.class)
    public void register(RegisterRequestDTO registerRequestDTO) {
        captcha(registerRequestDTO.getRecaptchaToken(),"register");
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

    private void captcha(String recaptchaToken,String action) {
        ReCaptchaResponseDTO captchaResponse = reCaptchaService.verify(recaptchaToken);

        if (captchaResponse == null || !captchaResponse.isSuccess()) {
            throw new AuthManagerException(ErrorType.RECAPTCHA_FAILED);
        }

        if (!action.equals(captchaResponse.getAction())) {
            throw new AuthManagerException(ErrorType.RECAPTCHA_FAILED);
        }

        if (captchaResponse.getScore() < 0.5) {
            throw new AuthManagerException(ErrorType.RECAPTCHA_FAILED);
        }
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
    public boolean saveVerifiedAccountEmail(String email) {
        Auth existingAuth = authRepository.findByEmailIgnoreCase(email).orElse(null);
        if (existingAuth != null) {
            existingAuth.setApproved(true);
            this.authRepository.save(existingAuth);
            return true;
        } else {
            return false;
        }

    }

    public ForgotPasswordResponseDTO createForgotPassword(String email) {
        try {
            Auth auth = this.authRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new AuthManagerException(ErrorType.EMAIL_NOT_FOUND));

            mailManager.createForgotPassword(ForgotPasswordRequestDTO.builder()
                    .email(auth.getEmail())
                    .authId(auth.getId())
                    .build());

            Long expirySeconds = redisTemplate.getExpire("reset_password:" + auth.getId(), TimeUnit.SECONDS);
            Instant expiryTime = Instant.now().plusSeconds(expirySeconds);

            return ForgotPasswordResponseDTO.builder().email(auth.getEmail()).expiryTime(expiryTime).build();
        } catch (AuthManagerException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthManagerException(ErrorType.INTERNAL_SERVER_ERROR);
        }
    }

    public CheckOtpResponseDTO checkOtp(CheckOtpRequestDTO checkOtpRequestDTO) {
        captcha(checkOtpRequestDTO.getRecaptchaToken(),"checkOtp");
        Auth auth = this.authRepository.findByEmailIgnoreCase(checkOtpRequestDTO.getEmail())
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
        if (!storedEmail.equals(checkOtpRequestDTO.getEmail())) {
            throw new AuthManagerException(ErrorType.EMAIL_MISMATCH);
        }

        if (attempts >= attemptsLimit) {
            redisTemplate.delete(redisKey);
            throw new AuthManagerException(ErrorType.TOO_MANY_ATTEMPTS);
        }

        // OTP doğrulama
        if (storedOtp != null && storedOtp.equals(checkOtpRequestDTO.getOtp())) {
            String passwordResetToken = generateSecureToken();

            resetData.put("reset_token", passwordResetToken);
            resetData.put("otp_verified", "true");
            resetData.put("verified_at", Instant.now().toString());

            redisTemplate.opsForHash().putAll(redisKey, resetData);
            redisTemplate.expire(redisKey, Duration.ofMinutes(5)); // 5 dakika daha
            Long expirySeconds = redisTemplate.getExpire("reset_password:" + auth.getId(), TimeUnit.SECONDS);
            Instant expiryTime = Instant.now().plusSeconds(expirySeconds);

            return CheckOtpResponseDTO.builder().resetToken(passwordResetToken).email(auth.getEmail()).expiryTime(expiryTime).success(true).build();
        } else {
            redisTemplate.opsForHash().put(redisKey, "attempts", String.valueOf(attempts + 1));

            if (attempts + 1 >= 3) {
                redisTemplate.delete(redisKey);
            }
            int remainingAttempts = attemptsLimit - (attempts + 1);
            return CheckOtpResponseDTO.builder().email(auth.getEmail()).remainingAttempts(remainingAttempts).success(false).message("Invalid OTP Code. Remaining attempts: " + remainingAttempts).build();
        }
    }
    @Transactional
    public void resetPassword(ForgotPasswordResetPasswordRequestDTO forgotPasswordResetPasswordRequestDTO) {
        captcha(forgotPasswordResetPasswordRequestDTO.getRecaptchaToken(),"resetPassword");
        Auth auth = this.authRepository.findAuthByAndEmailIgnoreCase(forgotPasswordResetPasswordRequestDTO.getEmail())
                .orElseThrow(() -> new AuthManagerException(ErrorType.EMAIL_NOT_FOUND));

        String redisKey = "reset_password:" + auth.getId();

        Map<Object, Object> resetData = redisTemplate.opsForHash().entries(redisKey);

        if (resetData.isEmpty()) {
            throw new AuthManagerException(ErrorType.PASSWORD_RESET_FAILED);
        }
        String storedEmail = (String) resetData.get("email");
        String resetToken = (String) resetData.get("reset_token");
        String otpVerified = (String) resetData.get("otp_verified");

        if (!storedEmail.equals(forgotPasswordResetPasswordRequestDTO.getEmail())) {
            throw new AuthManagerException(ErrorType.EMAIL_MISMATCH);
        }

        if (!resetToken.equals(forgotPasswordResetPasswordRequestDTO.getResetToken())) {
            throw new AuthManagerException(ErrorType.INVALID_RESET_TOKEN);
        }

        if (!"true".equals(otpVerified)) {
            throw new AuthManagerException(ErrorType.OTP_NOT_VERIFIED);
        }
        String newPassword = this.passwordEncoder.encode(forgotPasswordResetPasswordRequestDTO.getNewPassword());

        auth.setPassword(newPassword);
        this.authRepository.save(auth);

        try {
//
//            byte[] decryptedPrivateKeyBytes = decryptWithMasterKey(userKeyDTO.getEncryptedPrivateKeyWithMasterKey());
//            byte[] newSalt = generateRandomBytes(16);
//            byte[] newIv = generateRandomBytes(12);
//            SecretKey newAesKey = deriveAESKeyFromPassword(forgotPasswordResetPasswordRequestDTO.getNewPassword(), newSalt);
//
//            byte[] encryptedPrivateKey = encryptPrivateKey(decryptedPrivateKeyBytes, newAesKey, newIv);
//
//            userKeyDTO.setEncryptedPrivateKey(Base64.getEncoder().encodeToString(encryptedPrivateKey));
//            userKeyDTO.setIv(Base64.getEncoder().encodeToString(newIv));
//            userKeyDTO.setSalt(Base64.getEncoder().encodeToString(newSalt));
//            userKeyDTO.setEncryptedPrivateKeyWithMasterKey(null);

            iUserManager.resetUserKey(ResetUserKeyDTO.builder()
                    .userId(auth.getId())
                    .iv(forgotPasswordResetPasswordRequestDTO.getIv())
                    .encryptedPrivateKey(forgotPasswordResetPasswordRequestDTO.getEncryptedPrivateKey())
                    .salt(forgotPasswordResetPasswordRequestDTO.getSalt())
                    .publicKey(forgotPasswordResetPasswordRequestDTO.getPublicKey())
                    .build());
        } catch (FeignException e) {
            throw new AuthManagerException(ErrorType.USER_KEY_RESET_FAILED);
        }

    }

//    public String encryptWithMasterKey(String privateKey) {
//        try {
//            String masterKey = System.getenv("MASTER_KEY");
//            byte[] salt = generateRandomBytes(16);
//            byte[] iv = generateRandomBytes(12);
//
//            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
//            KeySpec spec = new PBEKeySpec(masterKey.toCharArray(), salt, 100, 256);
//            SecretKey tmp = factory.generateSecret(spec);
//            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
//
//            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
//            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));
//            byte[] encryptedText = cipher.doFinal(privateKey.getBytes(StandardCharsets.UTF_8));
//
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            outputStream.write(salt);
//            outputStream.write(iv);
//            outputStream.write(encryptedText);
//
//            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
//        } catch (Exception e) {
//            throw new RuntimeException("Private key encryption failed", e);
//        }
//    }
//
//    public SecretKey deriveAESKeyFromPassword(String password, byte[] salt) {
//        try {
//            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
//            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 100, 256);
//            SecretKey tmp = factory.generateSecret(spec);
//            return new SecretKeySpec(tmp.getEncoded(), "AES");
//        } catch (Exception e) {
//            throw new RuntimeException("AES key derivation failed", e);
//        }
//    }
//
//    public byte[] encryptPrivateKey(byte[] privateKeyBytes, SecretKey aesKey, byte[] iv) {
//        try {
//            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
//            if (iv.length != 12) {
//                throw new IllegalArgumentException("IV length for GCM must be 12 bytes");
//            }
//            cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
//            return cipher.doFinal(privateKeyBytes);
//        } catch (Exception e) {
//            throw new RuntimeException("Private key encryption failed", e);
//        }
//    }
//
//    public byte[] decryptWithMasterKey(String base64EncryptedData) {
//        try {
//            byte[] encryptedData = Base64.getDecoder().decode(base64EncryptedData);
//            String masterKey = System.getenv("MASTER_KEY");
//
//            byte[] salt = Arrays.copyOfRange(encryptedData, 0, 16);
//            byte[] iv = Arrays.copyOfRange(encryptedData, 16, 28);
//            byte[] encryptedPrivateKey = Arrays.copyOfRange(encryptedData, 28, encryptedData.length);
//
//            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
//            KeySpec spec = new PBEKeySpec(masterKey.toCharArray(), salt, 100, 256);
//            SecretKey tmp = factory.generateSecret(spec);
//            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
//
//            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
//            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));
//
//            return cipher.doFinal(encryptedPrivateKey);
//        } catch (Exception e) {
//            throw new RuntimeException("Private key decryption failed: " + e.getMessage(), e);
//        }
//    }
//
//
//    public static byte[] generateRandomBytes(int length) {
//        byte[] bytes = new byte[length];
//        SecureRandom secureRandom = new SecureRandom();
//        secureRandom.nextBytes(bytes);
//        return bytes;
//    }
//
    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }



}
