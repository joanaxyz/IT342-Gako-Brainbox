package edu.cit.gako.brainbox.notebook.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotebookRequest {
    private String title;
    private Long categoryId;
    private String content;
    private Long baseVersion;
    private String clientMutationId;
}
