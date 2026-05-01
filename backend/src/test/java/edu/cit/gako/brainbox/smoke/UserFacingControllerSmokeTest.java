package edu.cit.gako.brainbox.smoke;

import edu.cit.gako.brainbox.modules.ai.config.controller.AiConfigController;
import edu.cit.gako.brainbox.modules.ai.config.dto.response.AiConfigListResponse;
import edu.cit.gako.brainbox.modules.ai.config.dto.response.AiConfigResponse;
import edu.cit.gako.brainbox.modules.ai.config.service.AiConfigService;
import edu.cit.gako.brainbox.modules.auth.controller.AuthController;
import edu.cit.gako.brainbox.modules.auth.dto.response.LoginResponse;
import edu.cit.gako.brainbox.modules.auth.service.AuthFacade;
import edu.cit.gako.brainbox.modules.auth.service.JWTService;
import edu.cit.gako.brainbox.modules.flashcard.dto.response.FlashcardResponse;
import edu.cit.gako.brainbox.modules.flashcard.controller.FlashcardController;
import edu.cit.gako.brainbox.modules.flashcard.service.FlashcardService;
import edu.cit.gako.brainbox.modules.notebook.dto.response.NotebookFullResponse;
import edu.cit.gako.brainbox.modules.notebook.controller.NotebookController;
import edu.cit.gako.brainbox.modules.notebook.service.NotebookService;
import edu.cit.gako.brainbox.modules.playbackqueue.dto.response.PlaybackQueueResponse;
import edu.cit.gako.brainbox.modules.playbackqueue.controller.PlaybackQueueController;
import edu.cit.gako.brainbox.modules.playbackqueue.service.PlaybackQueueService;
import edu.cit.gako.brainbox.modules.quiz.dto.response.QuizResponse;
import edu.cit.gako.brainbox.modules.quiz.controller.QuizController;
import edu.cit.gako.brainbox.modules.quiz.service.QuizService;
import edu.cit.gako.brainbox.modules.user.dto.response.ProfileResponse;
import edu.cit.gako.brainbox.modules.user.controller.ProfileController;
import edu.cit.gako.brainbox.modules.user.service.ProfileService;
import edu.cit.gako.brainbox.platform.security.interceptor.AuthInterceptor;
import edu.cit.gako.brainbox.platform.security.interceptor.NotebookCacheInterceptor;
import edu.cit.gako.brainbox.shared.exception.GlobalExceptionHandler;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserFacingControllerSmokeTest {

    @Mock
    private AuthFacade authFacade;

    @Mock
    private ProfileService profileService;

    @Mock
    private NotebookService notebookService;

    @Mock
    private QuizService quizService;

    @Mock
    private FlashcardService flashcardService;

    @Mock
    private PlaybackQueueService playbackQueueService;

    @Mock
    private AiConfigService aiConfigService;

    @Mock
    private JWTService jwtService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        AuthController authController = new AuthController(authFacade);
        Field frontendUrl = AuthController.class.getDeclaredField("frontendUrl");
        frontendUrl.setAccessible(true);
        frontendUrl.set(authController, "http://localhost:3000");

        mockMvc = MockMvcBuilders.standaloneSetup(
                        authController,
                        new ProfileController(profileService),
                        new NotebookController(notebookService),
                        new PlaybackQueueController(playbackQueueService),
                        new AiConfigController(aiConfigService),
                        new QuizController(quizService),
                        new FlashcardController(flashcardService))
                .addInterceptors(new AuthInterceptor(jwtService), new NotebookCacheInterceptor())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void loginRouteStillWorksWithoutAuth() throws Exception {
        LoginResponse response = new LoginResponse();
        response.setAccessToken("access");
        response.setRefreshToken("refresh");
        when(authFacade.login(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"demo","password":"secret"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access"));
    }

    @Test
    void profileRoutesWorkForAuthenticatedUser() throws Exception {
        authorizeUser();

        ProfileResponse response = new ProfileResponse();
        response.setUsername("alice");
        response.setEmail("alice@example.com");
        response.setCreatedAt(Instant.parse("2026-04-18T00:00:00Z"));
        when(profileService.getProfile(11L)).thenReturn(response);

        mockMvc.perform(get("/api/user/me").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("alice"));

        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("alice"));
    }

    @Test
    void refreshTokenRoutesWork() throws Exception {
        LoginResponse response = new LoginResponse();
        response.setAccessToken("next-access");
        response.setRefreshToken("refresh");
        when(authFacade.refreshToken("refresh")).thenReturn(response);

        mockMvc.perform(post("/api/auth/refresh-token?refreshToken=refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("next-access"));

        mockMvc.perform(post("/api/auth/tokens/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"refresh"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("next-access"));
    }

    @Test
    void notebookCreateRouteStillWorks() throws Exception {
        authorizeUser();
        NotebookFullResponse response = new NotebookFullResponse();
        response.setUuid("nb-1");
        response.setTitle("Notebook");
        response.setContent("<p>Hello</p>");
        response.setVersion(0L);
        when(notebookService.createNotebook(any(), eq(11L))).thenReturn(response);

        mockMvc.perform(post("/api/notebooks")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Notebook","content":"<p>Hello</p>"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.uuid").value("nb-1"));
    }

    @Test
    void notebookReviewRoutesWork() throws Exception {
        authorizeUser();

        mockMvc.perform(patch("/api/notebooks/update-review/nb-1").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(patch("/api/notebooks/nb-1/review").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void playbackQueueRoutesWork() throws Exception {
        authorizeUser();
        PlaybackQueueResponse response = new PlaybackQueueResponse();
        response.setCurrentIndex(0);
        response.setItems(List.of());
        when(playbackQueueService.getQueue(11L)).thenReturn(response);

        mockMvc.perform(get("/api/queue").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentIndex").value(0));

        mockMvc.perform(get("/api/playback-queues/current").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentIndex").value(0));
    }

    @Test
    void aiConfigRoutesWork() throws Exception {
        authorizeUser();
        AiConfigResponse config = new AiConfigResponse();
        config.setId(5L);
        config.setName("Default");
        AiConfigListResponse listResponse = new AiConfigListResponse();
        listResponse.setSelectedConfigId(5L);
        listResponse.setConfigs(List.of(config));

        when(aiConfigService.getConfig(11L)).thenReturn(Optional.of(config));
        when(aiConfigService.listConfigs(11L)).thenReturn(listResponse);

        mockMvc.perform(get("/api/ai/config").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5));

        mockMvc.perform(get("/api/ai/configs/selected").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5));

        mockMvc.perform(get("/api/ai/config/list").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.selectedConfigId").value(5));

        mockMvc.perform(get("/api/ai/configs").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.selectedConfigId").value(5));
    }

    @Test
    void quizAttemptRouteStillWorks() throws Exception {
        authorizeUser();
        QuizResponse response = new QuizResponse();
        response.setUuid("quiz-1");
        response.setTitle("Quiz");
        response.setQuestions(List.of());
        response.setQuestionCount(0);
        when(quizService.recordAttempt(eq("quiz-1"), eq(11L), any())).thenReturn(response);

        mockMvc.perform(post("/api/quizzes/quiz-1/attempts")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"score":90}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.uuid").value("quiz-1"));
    }

    @Test
    void flashcardAttemptRouteStillWorks() throws Exception {
        authorizeUser();
        FlashcardResponse response = new FlashcardResponse();
        response.setUuid("flash-1");
        response.setTitle("Flashcard");
        response.setCards(List.of());
        response.setCardCount(0);
        when(flashcardService.recordAttempt(eq("flash-1"), eq(11L), any())).thenReturn(response);

        mockMvc.perform(post("/api/flashcards/flash-1/attempts")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"mastery":5}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.uuid").value("flash-1"));
    }

    private void authorizeUser() {
        when(jwtService.validateToken(anyString())).thenReturn(true);
        when(jwtService.extractUserId(anyString())).thenReturn(11L);
        when(jwtService.extractRole(anyString())).thenReturn("USER");
    }
}
