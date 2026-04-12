package edu.cit.gako.brainbox.notebook.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotebookMutationRequest {
    private Long baseVersion;
    private String clientMutationId;
}
