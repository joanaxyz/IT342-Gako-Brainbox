package edu.cit.gako.brainbox.ai.service;

import edu.cit.gako.brainbox.ai.dto.request.AiConfigRequest;
import edu.cit.gako.brainbox.ai.dto.response.AiConfigListResponse;
import edu.cit.gako.brainbox.ai.dto.response.AiConfigResponse;
import edu.cit.gako.brainbox.ai.entity.AiConfig;
import edu.cit.gako.brainbox.ai.repository.AiConfigRepository;
import edu.cit.gako.brainbox.auth.entity.User;
import edu.cit.gako.brainbox.auth.repository.UserRepository;
import edu.cit.gako.brainbox.auth.service.UserService;
import edu.cit.gako.brainbox.common.utils.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiConfigService {

    private final AiConfigRepository aiConfigRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;

    public Optional<AiConfigResponse> getConfig(Long userId) {
        return resolveActiveConfig(userId).map(this::mapToResponse);
    }

    @Transactional
    public AiConfigListResponse listConfigs(Long userId) {
        User user = userService.findById(userId);
        List<AiConfig> entities = aiConfigRepository.findByUser_IdOrderByUpdatedAtDesc(userId);
        syncUserSelection(user, entities);

        AiConfigListResponse out = new AiConfigListResponse();
        out.setConfigs(entities.stream().map(this::mapToResponse).collect(Collectors.toList()));
        user = userService.findById(userId);
        out.setSelectedConfigId(getSelectedConfigIdSafely(user));
        return out;
    }

    public AiConfig getConfigEntity(Long userId) {
        return resolveActiveConfig(userId)
            .orElseThrow(() -> new IllegalStateException("AI is not configured. Please add your configuration in settings."));
    }

    /**
     * Ensures the user's selected config points at a valid row and defaults the first config when unset
     * (e.g. after migrating from a single-config schema).
     */
    @Transactional
    Optional<AiConfig> resolveActiveConfig(Long userId) {
        User user = userService.findById(userId);
        List<AiConfig> entities = aiConfigRepository.findByUser_IdOrderByUpdatedAtDesc(userId);
        if (entities.isEmpty()) {
            if (user.getSelectedAiConfig() != null) {
                user.setSelectedAiConfig(null);
                userRepository.save(user);
            }
            return Optional.empty();
        }

        syncUserSelection(user, entities);
        user = userService.findById(userId);

        Long selectedId = getSelectedConfigIdSafely(user);

        if (selectedId == null) {
            return Optional.of(entities.get(0));
        }

        return entities.stream()
            .filter((c) -> c.getId().equals(selectedId))
            .findFirst()
            .or(() -> Optional.of(entities.get(0)));
    }

    private void syncUserSelection(User user, List<AiConfig> entities) {
        if (entities.isEmpty()) {
            if (user.getSelectedAiConfig() != null) {
                user.setSelectedAiConfig(null);
                userRepository.save(user);
            }
            return;
        }

        Long sid = getSelectedConfigIdSafely(user);
        if (sid != null) {
            boolean stillValid = entities.stream().anyMatch((c) -> c.getId().equals(sid));
            if (!stillValid) {
                user.setSelectedAiConfig(null);
                userRepository.save(user);
            }
        }

        if (user.getSelectedAiConfig() == null) {
            user.setSelectedAiConfig(entities.get(0));
            userRepository.save(user);
        }
    }

    @Transactional
    public AiConfigResponse saveConfig(AiConfigRequest request, Long userId) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Configuration name is required");
        }
        if (request.getModel() == null || request.getModel().isBlank()) {
            throw new IllegalArgumentException("Model is required");
        }
        if (request.getProxyUrl() == null || request.getProxyUrl().isBlank()) {
            throw new IllegalArgumentException("Proxy URL is required");
        }

        User user = userService.findById(userId);
        AiConfig config;

        if (request.getId() != null) {
            config = aiConfigRepository.findByIdAndUser_Id(request.getId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found"));
        } else {
            config = new AiConfig();
            config.setUser(user);
        }

        config.setName(request.getName().trim());
        config.setModel(request.getModel().trim());
        config.setProxyUrl(normalizeProxyUrl(request.getProxyUrl().trim()));

        if (request.getApiKey() != null && !request.getApiKey().isBlank()) {
            config.setApiKey(encryptionUtil.encrypt(request.getApiKey().trim()));
        } else if (config.getApiKey() == null) {
            throw new IllegalArgumentException("API key is required");
        }

        config = aiConfigRepository.save(config);

        if (user.getSelectedAiConfig() == null) {
            user.setSelectedAiConfig(config);
            userRepository.save(user);
        }

        return mapToResponse(config);
    }

    @Transactional
    public void deleteConfig(Long userId, Long configId) {
        AiConfig config = aiConfigRepository.findByIdAndUser_Id(configId, userId)
            .orElseThrow(() -> new IllegalArgumentException("No AI configuration found"));
        User user = userService.findById(userId);

        boolean wasSelected = user.getSelectedAiConfig() != null
            && user.getSelectedAiConfig().getId().equals(configId);

        aiConfigRepository.delete(config);

        if (wasSelected) {
            user.setSelectedAiConfig(null);
        }

        List<AiConfig> remaining = aiConfigRepository.findByUser_IdOrderByUpdatedAtDesc(userId);
        if (!remaining.isEmpty() && user.getSelectedAiConfig() == null) {
            user.setSelectedAiConfig(remaining.get(0));
        }
        userRepository.save(user);
    }

    @Transactional
    public AiConfigResponse selectConfig(Long userId, Long configId) {
        AiConfig config = aiConfigRepository.findByIdAndUser_Id(configId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Configuration not found"));
        User user = userService.findById(userId);
        user.setSelectedAiConfig(config);
        userRepository.save(user);
        return mapToResponse(config);
    }

    public String decryptApiKey(AiConfig config) {
        return encryptionUtil.decrypt(config.getApiKey());
    }

    private String normalizeProxyUrl(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private AiConfigResponse mapToResponse(AiConfig config) {
        AiConfigResponse response = new AiConfigResponse();
        response.setId(config.getId());
        response.setName(config.getName());
        response.setModel(config.getModel());
        response.setProxyUrl(config.getProxyUrl());
        response.setHasApiKey(config.getApiKey() != null && !config.getApiKey().isBlank());
        response.setCreatedAt(config.getCreatedAt());
        response.setUpdatedAt(config.getUpdatedAt());
        return response;
    }

    /**
     * Handles stale selected_ai_config_id references gracefully.
     */
    private Long getSelectedConfigIdSafely(User user) {
        if (user.getSelectedAiConfig() == null) {
            return null;
        }

        try {
            return user.getSelectedAiConfig().getId();
        } catch (EntityNotFoundException | org.hibernate.ObjectNotFoundException ignored) {
            return null;
        }
    }
}
