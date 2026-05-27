package edu.cit.gako.brainbox.modules.ai.config.dto.response;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiConfigResponse {
    private Long id;
    private String name;
    private String model;
    private String baseUrl;
    private String proxyUrl;
    private boolean hasApiKey;
    private Instant createdAt;
    private Instant updatedAt;
}
