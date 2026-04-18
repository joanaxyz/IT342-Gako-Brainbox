package edu.cit.gako.brainbox.notebook.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaybackQueueResponse {
    private List<NotebookOverviewResponse> items;
}
