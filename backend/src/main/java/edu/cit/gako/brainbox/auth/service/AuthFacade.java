package edu.cit.gako.brainbox.auth.service;

import edu.cit.gako.brainbox.auth.dto.request.LoginRequest;
import edu.cit.gako.brainbox.auth.dto.request.LogoutRequest;
import edu.cit.gako.brainbox.auth.dto.request.RegisterRequest;
import edu.cit.gako.brainbox.auth.dto.response.LoginResponse;
import edu.cit.gako.brainbox.auth.dto.response.VerifyCodeResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Facade Pattern: AuthFacade is the single entry point for all authentication
 * operations exposed to the presentation layer (AuthController).
 *
 * The facade hides the complexity of coordinating JWTService, UserService,
 * CodeService, RefreshTokenService, and EmailService. Controllers depend on
 * this interface rather than on the concrete AuthService implementation,
 * which improves testability and keeps HTTP concerns decoupled from business logic.
 */
public interface AuthFacade {

    void register(RegisterRequest request);

    void verifyEmail(String token);

    void forgotPassword(String email);

    VerifyCodeResponse verifyCode(String email, String code);

    void resetPassword(String token, String newPassword);

    LoginResponse login(LoginRequest request, HttpServletRequest servletRequest);

    void logout(LogoutRequest request);

    LoginResponse refreshToken(String refreshToken);

    LoginResponse googleLogin(String idToken, String accessToken, HttpServletRequest servletRequest);
}
