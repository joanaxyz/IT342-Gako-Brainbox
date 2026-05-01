package edu.cit.gako.brainbox.modules.ai.config.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiConfigListResponse {
    private List<AiConfigResponse> configs;
    private Long selectedConfigId;
}
