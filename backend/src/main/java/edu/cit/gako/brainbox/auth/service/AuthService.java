package edu.cit.gako.brainbox.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import edu.cit.gako.brainbox.auth.dto.request.LoginRequest;
import edu.cit.gako.brainbox.auth.dto.request.LogoutRequest;
import edu.cit.gako.brainbox.auth.dto.request.RegisterRequest;
import edu.cit.gako.brainbox.auth.dto.response.LoginResponse;
import edu.cit.gako.brainbox.auth.dto.response.VerifyCodeResponse;
import edu.cit.gako.brainbox.auth.entity.Code;
import edu.cit.gako.brainbox.auth.entity.RefreshToken;
import edu.cit.gako.brainbox.auth.entity.User;
import edu.cit.gako.brainbox.auth.enumeration.UserRole;
import edu.cit.gako.brainbox.email.EmailService;
import edu.cit.gako.brainbox.email.EmailTemplateService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Facade Pattern — concrete facade that orchestrates the authentication subsystem:
 * JWTService, UserService, CodeService, RefreshTokenService, and EmailService.
 * AuthController depends on the AuthFacade interface, not this class directly.
 */
@Service
@RequiredArgsConstructor
public class AuthService implements AuthFacade {
    private final JWTService jwtService;
    private final UserService userService;
    private final CodeService codeService;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;
    private final PasswordEncoder passwordEncoder;
    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
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
        Long userId = jwtService.extractUserId(token);
        User user = userService.findById(userId);
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
        Long userId = jwtService.extractUserId(token);
        userService.updatePassword(userId, passwordEncoder.encode(newPassword));
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

    @Transactional
    public LoginResponse googleLogin(String idToken, String accessToken, HttpServletRequest servletRequest) {
        String googleId, email, name;
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request;
            if (idToken != null && !idToken.isBlank()) {
                // Mobile: ID token flow — verify via tokeninfo
                request = HttpRequest.newBuilder()
                        .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken))
                        .GET()
                        .build();
            } else if (accessToken != null && !accessToken.isBlank()) {
                // Web: OAuth access token flow — fetch userinfo
                request = HttpRequest.newBuilder()
                        .uri(URI.create("https://www.googleapis.com/oauth2/v3/userinfo"))
                        .header("Authorization", "Bearer " + accessToken)
                        .GET()
                        .build();
            } else {
                throw new IllegalArgumentException("No Google token provided");
            }
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalArgumentException("Invalid Google token");
            }
            JsonNode json = new ObjectMapper().readTree(response.body());
            googleId = json.get("sub").asText();
            email = json.get("email").asText();
            name = json.has("name") ? json.get("name").asText() : null;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to verify Google token");
        }

        User user = userService.findByGoogleId(googleId).orElse(null);

        if (user == null) {
            // Check if a user with this email already exists (registered via email/password)
            user = userService.findByEmailOptional(email).orElse(null);

            if (user != null) {
                // Link Google account to existing user
                user.setGoogleId(googleId);
                user.setAuthProvider("google");
                if (!user.isVerified()) {
                    user.setVerified(true);
                }
                userService.save(user);
            } else {
                // Create new user
                String username = generateUniqueUsername(name != null ? name : email.split("@")[0]);
                user = User.builder()
                        .username(username)
                        .email(email)
                        .googleId(googleId)
                        .authProvider("google")
                        .verified(true)
                        .role(UserRole.USER)
                        .build();
                user = userService.save(user);
            }
        }

        LoginResponse response = new LoginResponse();
        response.setAccessToken(jwtService.generateAccessToken(user));

        String userAgent = servletRequest.getHeader("User-Agent");
        String ipAddress = servletRequest.getRemoteAddr();
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, userAgent, ipAddress);
        response.setRefreshToken(refreshToken.getToken());
        return response;
    }

    private String generateUniqueUsername(String baseName) {
        String username = baseName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        if (username.isEmpty()) username = "user";
        if (!userService.existsByUsername(username)) return username;
        int suffix = 1;
        while (userService.existsByUsername(username + suffix)) {
            suffix++;
        }
        return username + suffix;
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
