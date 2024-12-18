package com.vky.controller;

import com.vky.dto.request.AuthRequestDTO;
import com.vky.dto.request.CheckOtpRequestDTO;
import com.vky.dto.request.ForgotPasswordResetPasswordRequestDTO;
import com.vky.dto.response.*;
import com.vky.entity.Auth;
import com.vky.mapper.IAuthMapper;
import com.vky.repository.IAuthRepository;
import com.vky.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final IAuthRepository authRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody @Valid AuthRequestDTO authRequestDTO) {

        return ResponseEntity.ok(authService.register(authRequestDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid AuthRequestDTO authRequestDTO) {
        return ResponseEntity.ok(authService.doLoginn(authRequestDTO));
    }

    @GetMapping("/hello")
    public ResponseEntity<String> sayHello() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            Auth auth = (Auth) authentication.getPrincipal();
        } else {
        }
        return ResponseEntity.ok("Hello, ");
    }

    @GetMapping("/authenticate")
    public Boolean authenticate() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         return authentication != null && authentication.isAuthenticated() &&
                 !authentication.getName().equals("anonymousUser");
    }

//    @GetMapping("/load-user-by-username")
//    public UserDetails loadUserByUsername(@RequestParam String email) {
//        System.out.println("asdfasdfasdf");
//        return authService.loadUserByUsername(email);
//    }


    @GetMapping("/username")
    public Object getUsername(Authentication authentication) {
        return authentication.getPrincipal();
    }


    @GetMapping("/find-by-id-with-tokens/{id}")
    public ResponseEntity<AuthWithTokensDTO> findByIdWithTokens(@PathVariable UUID id) {
        Auth auth = authService.findById(id);
        if (auth != null) {

            List<TokenDTO> tokenDTOs = auth.getTokens().stream()
                    .map(token -> TokenDTO.builder()
                            .id(token.getId())
                            .token(token.getToken())
                            .revoked(token.isRevoked())
                            .expired(token.isExpired())
                            .build())
                    .collect(Collectors.toList());
            AuthWithTokensDTO authWithTokensDTO = AuthWithTokensDTO.builder()
                    .id(auth.getId())
                    .email(auth.getEmail())
                    .role(auth.getRole())
                    .isEnabled(auth.isEnabled())
                    .tokens(tokenDTOs)
                    .build();

            return ResponseEntity.ok(authWithTokensDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthDTO> getAuthById(@PathVariable UUID id) {
        Optional<Auth> authOptional = authRepository.findById(id);

        if (authOptional.isPresent()) {
            Auth auth = authOptional.get();
            AuthDTO authDTO = IAuthMapper.INSTANCE.toAuthDTO(auth);
            return ResponseEntity.ok(authDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping("/save-verified-account")
    public ResponseEntity<Void> saveVerifiedAccount(@RequestBody UUID id) {
        authService.saveVerifiedAccount(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("create-forgot-password")
    public ResponseEntity<HttpResponse> createForgotPassword(@RequestBody String email) {
        Optional<Auth> auth = authService.createForgotPassword(email);

        return auth.<ResponseEntity<HttpResponse>>map(data -> ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .data(Map.of("Success", true))
                        .message("Doğrulama Kodu Gönderildi")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build())).orElseGet(() -> ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .data(Map.of("Unsuccessful", false))
                        .message("Doğrulama Kodu Gönderilemedi")
                        .status(HttpStatus.BAD_REQUEST)
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build()));
    }

    @PostMapping("/check-otp")
    ResponseEntity<HttpResponse> checkOtp(@RequestBody CheckOtpRequestDTO checkOtpRequestDTO) {
        HttpResponse response = this.authService.findByEmailOtp(checkOtpRequestDTO);
        System.out.println("response: " + response);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/reset-password")
    ResponseEntity<HttpResponse> resetPassword(@RequestBody ForgotPasswordResetPasswordRequestDTO
                                                       forgotPasswordResetPasswordRequestDTO) {
        System.out.println("authService: " + forgotPasswordResetPasswordRequestDTO.toString());
        this.authService.resetPassword(forgotPasswordResetPasswordRequestDTO);
        return ResponseEntity.ok().build();
    }


}
