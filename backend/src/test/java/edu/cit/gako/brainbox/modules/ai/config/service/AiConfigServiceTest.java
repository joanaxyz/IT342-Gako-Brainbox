package edu.cit.gako.brainbox.modules.ai.config.service;

import edu.cit.gako.brainbox.modules.ai.config.dto.request.AiConfigRequest;
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
import static org.mockito.ArgumentMatchers.any;
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
        request.setProxyUrl("https://proxy.example.com/");
        request.setApiKey("secret");

        when(userService.findById(7L)).thenReturn(user);
        when(encryptionUtil.encrypt("secret")).thenReturn("encrypted-secret");
        when(aiConfigRepository.save(any(AiConfig.class))).thenAnswer((invocation) -> {
            AiConfig config = invocation.getArgument(0);
            config.setId(99L);
            return config;
        });

        aiConfigService.saveConfig(request, 7L);

        ArgumentCaptor<AiConfig> configCaptor = ArgumentCaptor.forClass(AiConfig.class);
        verify(userService).setSelectedAiConfig(any(User.class), configCaptor.capture());
        assertEquals(99L, configCaptor.getValue().getId());
        assertEquals("https://proxy.example.com", configCaptor.getValue().getProxyUrl());
        assertNotNull(configCaptor.getValue().getApiKey());
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
