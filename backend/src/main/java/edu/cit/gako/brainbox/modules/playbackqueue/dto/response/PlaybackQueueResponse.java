package edu.cit.gako.brainbox.modules.playbackqueue.dto.response;

import edu.cit.gako.brainbox.modules.notebook.dto.response.NotebookOverviewResponse;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaybackQueueResponse {
    private String playlistUuid;
    private String playlistTitle;
    private int currentIndex;
    private List<NotebookOverviewResponse> items;
}
