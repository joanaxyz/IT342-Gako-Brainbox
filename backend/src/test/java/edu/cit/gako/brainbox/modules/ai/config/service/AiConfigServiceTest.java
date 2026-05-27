package edu.cit.gako.brainbox.modules.ai.config.service;

import edu.cit.gako.brainbox.modules.ai.config.dto.request.AiConfigRequest;
import edu.cit.gako.brainbox.modules.ai.config.dto.response.AiConfigResponse;
import edu.cit.gako.brainbox.modules.ai.config.entity.AiConfig;
import edu.cit.gako.brainbox.modules.ai.config.repository.AiConfigRepository;
import edu.cit.gako.brainbox.modules.user.service.UserService;
import edu.cit.gako.brainbox.modules.user.entity.User;
import edu.cit.gako.brainbox.shared.util.EncryptionUtil;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiConfigServiceTest {

    @Mock
    private AiConfigRepository aiConfigRepository;

    @Mock
    private UserService userService;

    @Mock
    private EncryptionUtil encryptionUtil;

    @InjectMocks
    private AiConfigService aiConfigService;

    @Test
    void saveConfigAssignsFirstSelectionThroughUserService() {
        User user = new User();
        user.setId(7L);

        AiConfigRequest request = new AiConfigRequest();
        request.setName("Primary");
        request.setModel("gpt-5");
        request.setBaseUrl("https://api.openai.com/v1/");
        request.setApiKey("secret");

        when(userService.findById(7L)).thenReturn(user);
        when(encryptionUtil.encrypt("secret")).thenReturn("encrypted-secret");
        when(aiConfigRepository.save(any(AiConfig.class))).thenAnswer((invocation) -> {
            AiConfig config = invocation.getArgument(0);
            config.setId(99L);
            return config;
        });

        AiConfigResponse response = aiConfigService.saveConfig(request, 7L);

        ArgumentCaptor<AiConfig> configCaptor = ArgumentCaptor.forClass(AiConfig.class);
        verify(userService).setSelectedAiConfig(any(User.class), configCaptor.capture());
        assertEquals(99L, configCaptor.getValue().getId());
        assertEquals("https://api.openai.com/v1", configCaptor.getValue().getProxyUrl());
        assertEquals("https://api.openai.com/v1", response.getBaseUrl());
        assertEquals("https://api.openai.com/v1", response.getProxyUrl());
        assertNotNull(configCaptor.getValue().getApiKey());
    }

    @Test
    void saveConfigAcceptsLegacyProxyUrlPayload() {
        User user = new User();
        user.setId(7L);

        AiConfigRequest request = new AiConfigRequest();
        request.setName("Legacy");
        request.setModel("gpt-4o-mini");
        request.setProxyUrl("https://openrouter.ai/api/v1");
        request.setApiKey("secret");

        when(userService.findById(7L)).thenReturn(user);
        when(encryptionUtil.encrypt("secret")).thenReturn("encrypted-secret");
        when(aiConfigRepository.save(any(AiConfig.class))).thenAnswer((invocation) -> invocation.getArgument(0));

        AiConfigResponse response = aiConfigService.saveConfig(request, 7L);

        assertEquals("https://openrouter.ai/api/v1", response.getBaseUrl());
        assertEquals("https://openrouter.ai/api/v1", response.getProxyUrl());
    }

    @Test
    void saveConfigNormalizesFullChatCompletionsUrlToBaseUrl() {
        User user = new User();
        user.setId(7L);

        AiConfigRequest request = new AiConfigRequest();
        request.setName("Gemini");
        request.setModel("gemini-2.0-flash");
        request.setBaseUrl(" https://generativelanguage.googleapis.com/v1beta/openai/chat/completions/ ");
        request.setApiKey("secret");

        when(userService.findById(7L)).thenReturn(user);
        when(encryptionUtil.encrypt("secret")).thenReturn("encrypted-secret");
        when(aiConfigRepository.save(any(AiConfig.class))).thenAnswer((invocation) -> invocation.getArgument(0));

        AiConfigResponse response = aiConfigService.saveConfig(request, 7L);

        assertEquals("https://generativelanguage.googleapis.com/v1beta/openai", response.getBaseUrl());
    }

    @Test
    void saveConfigRejectsBlankBaseUrl() {
        AiConfigRequest request = new AiConfigRequest();
        request.setName("Blank");
        request.setModel("gpt-4o-mini");
        request.setBaseUrl(" ");
        request.setApiKey("secret");

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> aiConfigService.saveConfig(request, 7L)
        );

        assertEquals("API Base URL is required", error.getMessage());
    }

    @Test
    void saveConfigRejectsBaseUrlWithoutHttpScheme() {
        AiConfigRequest request = new AiConfigRequest();
        request.setName("Bad URL");
        request.setModel("gpt-4o-mini");
        request.setBaseUrl("api.openai.com/v1");
        request.setApiKey("secret");

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> aiConfigService.saveConfig(request, 7L)
        );

        assertEquals("API Base URL must start with http:// or https://", error.getMessage());
    }

    @Test
    void saveConfigKeepsExistingEncryptedApiKeyWhenEditingWithoutNewKey() {
        User user = new User();
        user.setId(7L);

        AiConfig existing = new AiConfig();
        existing.setId(42L);
        existing.setUser(user);
        existing.setName("Existing");
        existing.setModel("gpt-4o-mini");
        existing.setProxyUrl("https://api.openai.com/v1");
        existing.setApiKey("encrypted-existing");

        AiConfigRequest request = new AiConfigRequest();
        request.setId(42L);
        request.setName("Updated");
        request.setModel("google/gemini-2.0-flash-001");
        request.setBaseUrl("https://openrouter.ai/api/v1");
        request.setApiKey("");

        when(userService.findById(7L)).thenReturn(user);
        when(aiConfigRepository.findByIdAndUser_Id(42L, 7L)).thenReturn(Optional.of(existing));
        when(aiConfigRepository.save(any(AiConfig.class))).thenAnswer((invocation) -> invocation.getArgument(0));

        aiConfigService.saveConfig(request, 7L);

        ArgumentCaptor<AiConfig> configCaptor = ArgumentCaptor.forClass(AiConfig.class);
        verify(aiConfigRepository).save(configCaptor.capture());
        assertEquals("encrypted-existing", configCaptor.getValue().getApiKey());
        assertEquals("https://openrouter.ai/api/v1", configCaptor.getValue().getProxyUrl());
        verify(encryptionUtil, never()).encrypt(any());
    }

    @Test
    void deleteConfigAsAdminReassignsFallbackSelection() {
        User owner = new User();
        owner.setId(12L);

        AiConfig deleted = new AiConfig();
        deleted.setId(1L);
        deleted.setUser(owner);
        owner.setSelectedAiConfig(deleted);

        AiConfig fallback = new AiConfig();
        fallback.setId(2L);
        fallback.setUser(owner);

        when(aiConfigRepository.findById(1L)).thenReturn(Optional.of(deleted));
        when(aiConfigRepository.findByUser_IdOrderByUpdatedAtDesc(12L)).thenReturn(List.of(fallback));

        aiConfigService.deleteConfigAsAdmin(1L);

        verify(aiConfigRepository).delete(deleted);
        verify(userService).clearSelectedAiConfig(owner);
        verify(userService).setSelectedAiConfig(owner, fallback);
    }
}
