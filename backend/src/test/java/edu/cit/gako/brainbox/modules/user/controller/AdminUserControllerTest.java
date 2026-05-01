package edu.cit.gako.brainbox.modules.user.controller;

import edu.cit.gako.brainbox.modules.auth.service.JWTService;
import edu.cit.gako.brainbox.modules.user.service.UserService;
import edu.cit.gako.brainbox.platform.security.interceptor.AuthInterceptor;
import edu.cit.gako.brainbox.platform.security.interceptor.NotebookCacheInterceptor;
import edu.cit.gako.brainbox.shared.exception.GlobalExceptionHandler;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JWTService jwtService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminUserController(userService))
                .addInterceptors(new AuthInterceptor(jwtService), new NotebookCacheInterceptor())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void rejectsUnauthenticatedAdminRequest() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsNonAdminRequest() throws Exception {
        when(jwtService.validateToken(anyString())).thenReturn(true);
        when(jwtService.extractUserId(anyString())).thenReturn(7L);
        when(jwtService.extractRole(anyString())).thenReturn("USER");

        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsAdminRequest() throws Exception {
        when(jwtService.validateToken(anyString())).thenReturn(true);
        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(jwtService.extractRole(anyString())).thenReturn("ADMIN");
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
}
