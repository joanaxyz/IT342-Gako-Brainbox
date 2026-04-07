package edu.cit.gako.brainbox.notebook.dto.response;
import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotebookVersionResponse {
    private Long id;
    private String content;
    private Instant version;
}
