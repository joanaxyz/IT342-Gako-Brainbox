package edu.cit.gako.brainbox.modules.playlist.dto.response;

import edu.cit.gako.brainbox.modules.notebook.dto.response.NotebookOverviewResponse;
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
