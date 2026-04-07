package edu.cit.gako.brainbox.notebook.dto.request;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaylistReorderRequest {
    private List<String> notebookUuids;
}
