package com.example.brainbox_api.notebook.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReorderRequest {
    private Long draggedId;
    private Long targetId;
}
