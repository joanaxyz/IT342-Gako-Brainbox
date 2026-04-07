package edu.cit.gako.brainbox.ai.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AiConfigListResponse {
    private List<AiConfigResponse> configs;
    private Long selectedConfigId;
}
