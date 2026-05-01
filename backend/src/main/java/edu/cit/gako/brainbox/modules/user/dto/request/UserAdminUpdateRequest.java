package edu.cit.gako.brainbox.modules.user.dto.request;

import edu.cit.gako.brainbox.modules.user.entity.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAdminUpdateRequest {
    private String username;
    private String email;
    private UserRole role;
    private Boolean banned;
    private Boolean verified;
}
