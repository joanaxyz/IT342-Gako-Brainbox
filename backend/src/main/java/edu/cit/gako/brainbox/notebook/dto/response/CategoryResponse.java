package com.example.brainbox_api.notebook.dto.response;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryResponse {
    private Long id;
    private String name;
    private Instant createdAt;
    private Instant updatedAt;
}
