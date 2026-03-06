package com.example.brainbox_api.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.brainbox_api.auth.dto.request.LoginRequest;
import com.example.brainbox_api.auth.dto.request.LogoutRequest;
import com.example.brainbox_api.auth.dto.request.RegisterRequest;
import com.example.brainbox_api.auth.dto.response.LoginResponse;
import com.example.brainbox_api.auth.dto.response.VerifyCodeResponse;
import com.example.brainbox_api.auth.entity.Code;
import com.example.brainbox_api.auth.entity.RefreshToken;
import com.example.brainbox_api.auth.entity.User;
import com.example.brainbox_api.auth.enumeration.UserRole;
import com.example.brainbox_api.email.EmailService;
import com.example.brainbox_api.email.EmailTemplateService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JWTService jwtService;
    private final UserService userService;
    private final CodeService codeService;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;
    private final PasswordEncoder passwordEncoder;
    @Value("${app.base-url}")
    private String baseUrl;

    public void register(RegisterRequest request){
        String username = request.getUsername();
        String email = request.getEmail();
        userService.validateUniqueness(username, email);
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.builder()
        .username(username)
        .email(email)
        .password(hashedPassword)
        .role(UserRole.USER)
        .build();
        User savedUser = userService.create(user);
        String token = jwtService.generateInvitationToken(savedUser);
        String verificationLink = baseUrl + "/api/auth/verify-email?token=" + token;
        emailService.sendEmail(request.getEmail(), "Account Verification", 
        emailTemplateService.buildVerificationEmail(username, verificationLink));
    }

    public void verifyEmail(String token){
        Long id = jwtService.extractUserId(token);
        User user = userService.findById(id);
        userService.verify(user);
    }

    public void forgotPassword(String email){
        User user = userService.findByEmail(email);
        String code = codeService.generateCode(user, 6, 300000);
        emailService.sendEmail(email, "Password Reset", 
        emailTemplateService.buildPasswordResetEmail(user.getUsername(), code));
    }

    public VerifyCodeResponse verifyCode(String email, String codeString){
        User user = userService.findByEmail(email);
        Code code = codeService.findByUser(user);
        if(!passwordEncoder.matches(codeString, code.getCode())){
            throw new IllegalArgumentException("Invalid verification code");
        }

        if(code.isExpired()){
            codeService.delete(code);
            throw new IllegalStateException("Verification code has expired");
        }
        
        String resetToken = jwtService.generateResetToken(user);
        codeService.delete(code);
        
        VerifyCodeResponse response = new VerifyCodeResponse();
        response.setResetToken(resetToken);
        return response;
    }

    public void resetPassword(String token, String newPassword){
        if (!jwtService.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }
        Long id = jwtService.extractUserId(token);
        userService.updatePassword(id, passwordEncoder.encode(newPassword));
    }

    public LoginResponse login(LoginRequest request, HttpServletRequest servletRequest){
        User user = userService.findByUsernameOrEmail(request.getUsername());
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword()) ||
            !user.isVerified()){
            throw new IllegalArgumentException("Invalid username or password");
        }
        LoginResponse response = new LoginResponse();
        response.setAccessToken(jwtService.generateAccessToken(user));
        
        String userAgent = servletRequest.getHeader("User-Agent");
        String ipAddress = servletRequest.getRemoteAddr();
        
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, userAgent, ipAddress);
        response.setRefreshToken(refreshToken.getToken());
        return response;
    }

    public void logout(LogoutRequest request) {
        refreshTokenService.deleteByToken(request.getRefreshToken());
    }

    public LoginResponse refreshToken(String refreshTokenString) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenString);
        refreshTokenService.verifyExpiration(refreshToken);
        User user = refreshToken.getUser();
        String accessToken = jwtService.generateAccessToken(user);
        
        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshTokenString);
        return response;
    }
}
