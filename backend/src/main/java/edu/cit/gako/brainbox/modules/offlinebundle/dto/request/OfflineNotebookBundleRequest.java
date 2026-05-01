package edu.cit.gako.brainbox.modules.offlinebundle.dto.request;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OfflineNotebookBundleRequest {
    private List<String> notebookUuids;
}
