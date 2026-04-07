package edu.cit.gako.brainbox.notebook.dto.response;

import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaylistResponse {
    private String uuid;
    private String title;
    private int currentIndex;
    private Instant createdAt;
    private Instant updatedAt;
    private List<NotebookOverviewResponse> queue;
}
