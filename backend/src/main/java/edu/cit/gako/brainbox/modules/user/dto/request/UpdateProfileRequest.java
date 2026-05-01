package edu.cit.gako.brainbox.modules.user.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {
    private String username;
    private String email;
}
