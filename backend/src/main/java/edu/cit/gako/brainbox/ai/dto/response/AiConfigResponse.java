package edu.cit.gako.brainbox.ai.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class AiConfigResponse {
    private Long id;
    private String name;
    private String model;
    private String proxyUrl;
    private boolean hasApiKey;
    private Instant createdAt;
    private Instant updatedAt;
}
