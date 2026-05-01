package edu.cit.gako.brainbox.modules.offlinebundle.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OfflineNotebookBundleResponse {
    private List<OfflineNotebookBundleItem> notebooks;
    private List<String> missingUuids;
}
