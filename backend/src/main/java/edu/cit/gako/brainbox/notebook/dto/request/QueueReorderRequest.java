package edu.cit.gako.brainbox.notebook.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QueueReorderRequest {
    private List<String> notebookUuids;
}
