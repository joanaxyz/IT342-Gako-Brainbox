package edu.cit.gako.brainbox.modules.user.dto.response;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileResponse {
    String username, email;
    Instant createdAt;
}
