package edu.cit.gako.brainbox.modules.user.dto.response;

import edu.cit.gako.brainbox.modules.user.entity.UserRole;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAdminResponse {
    private Long id;
    private String username;
    private String email;
    private UserRole role;
    private boolean banned;
    private boolean verified;
    private String authProvider;
    private Instant createdAt;
    private Instant lastLogin;
    private Instant lastLogout;
}
