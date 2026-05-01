package edu.cit.gako.brainbox.platform.security.interceptor;

import edu.cit.gako.brainbox.platform.security.annotation.RequireAuth;
import edu.cit.gako.brainbox.platform.security.annotation.RequireRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class NotebookCacheInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireAuth requireAuth = handlerMethod.getMethodAnnotation(RequireAuth.class);
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireAuth == null && requireRole == null) {
            return true;
        }

        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        return true;
    }
}
