package com.example.brainbox_api.auth.interceptor;

import com.example.brainbox_api.auth.annotation.RequireAuth;
import com.example.brainbox_api.auth.service.JWTService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JWTService jwtService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireAuth requireAuth = handlerMethod.getMethodAnnotation(RequireAuth.class);

        if (requireAuth == null) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Missing or invalid Authorization header\"}");
            response.setContentType("application/json");
            return false;
        }

        // checks if access token is valid, else returns a 401 error response.
        String token = authHeader.substring(7);
        if (!jwtService.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Invalid or expired token\"}");
            response.setContentType("application/json");
            return false;
        }

        request.setAttribute("userId", jwtService.extractUserId(token));
        request.setAttribute("userRole", jwtService.extractRole(token));

        return true;
    }
}
