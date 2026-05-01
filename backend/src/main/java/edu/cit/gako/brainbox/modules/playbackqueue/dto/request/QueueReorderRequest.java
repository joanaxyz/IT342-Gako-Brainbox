package edu.cit.gako.brainbox.modules.playbackqueue.dto.request;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueueReorderRequest {
    private List<String> notebookUuids;
}
