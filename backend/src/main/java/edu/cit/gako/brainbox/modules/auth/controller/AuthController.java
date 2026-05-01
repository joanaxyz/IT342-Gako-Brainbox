package edu.cit.gako.brainbox.modules.auth.controller;

import edu.cit.gako.brainbox.modules.auth.dto.request.ForgotPasswordRequest;
import edu.cit.gako.brainbox.modules.auth.dto.request.GoogleAuthRequest;
import edu.cit.gako.brainbox.modules.auth.dto.request.LoginRequest;
import edu.cit.gako.brainbox.modules.auth.dto.request.LogoutRequest;
import edu.cit.gako.brainbox.modules.auth.dto.request.RefreshTokenRequest;
import edu.cit.gako.brainbox.modules.auth.dto.request.RegisterRequest;
import edu.cit.gako.brainbox.modules.auth.dto.request.ResetPasswordRequest;
import edu.cit.gako.brainbox.modules.auth.dto.request.VerifyCodeRequest;
import edu.cit.gako.brainbox.modules.auth.dto.response.LoginResponse;
import edu.cit.gako.brainbox.modules.auth.dto.response.VerifyCodeResponse;
import edu.cit.gako.brainbox.modules.auth.service.AuthFacade;
import edu.cit.gako.brainbox.shared.controller.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthFacade authService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(frontendUrl + "/login?verified=true"))
            .build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponse<VerifyCodeResponse>> verifyCode(@RequestBody VerifyCodeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.verifyCode(request.getEmail(), request.getCode())));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request, servletRequest)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping({"/refresh-token", "/tokens/refresh"})
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @RequestParam(required = false) String refreshToken,
            @RequestBody(required = false) RefreshTokenRequest request) {
        String token = refreshToken != null ? refreshToken : request != null ? request.getRefreshToken() : null;
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }
        return ResponseEntity.ok(ApiResponse.success(authService.refreshToken(token)));
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<LoginResponse>> googleAuth(@RequestBody GoogleAuthRequest request, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success(authService.googleLogin(request.getIdToken(), request.getAccessToken(), servletRequest)));
    }
}
