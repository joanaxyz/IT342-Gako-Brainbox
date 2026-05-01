package edu.cit.gako.brainbox.modules.category.dto.response;

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
