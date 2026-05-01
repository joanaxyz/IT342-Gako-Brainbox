package edu.cit.gako.brainbox.modules.playlist.dto.request;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaylistReorderRequest {
    private List<String> notebookUuids;
}
