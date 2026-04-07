package edu.cit.gako.brainbox.auth.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyCodeResponse {
    private String resetToken;
}
