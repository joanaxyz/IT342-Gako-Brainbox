package edu.cit.gako.brainbox.profile.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ProfileResponse {
    String username, email;
    Instant createdAt;
}
