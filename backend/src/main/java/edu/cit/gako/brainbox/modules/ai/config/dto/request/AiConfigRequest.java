package edu.cit.gako.brainbox.modules.ai.config.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiConfigRequest {
    /** When set, updates an existing configuration; when null, creates a new one. */
    private Long id;
    private String name;
    private String model;
    private String proxyUrl;
    private String apiKey;
}
