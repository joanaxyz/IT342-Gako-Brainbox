package edu.cit.gako.brainbox.modules.auth.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequest {
    private String email;
}
