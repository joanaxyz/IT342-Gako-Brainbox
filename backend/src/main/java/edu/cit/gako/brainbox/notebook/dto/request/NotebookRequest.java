package com.example.brainbox_api.notebook.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotebookRequest {
    private String title;
    private String content;
    private Long categoryId;
}
