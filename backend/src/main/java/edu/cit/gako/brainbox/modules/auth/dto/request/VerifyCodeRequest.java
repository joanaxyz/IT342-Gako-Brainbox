package edu.cit.gako.brainbox.modules.auth.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyCodeRequest {
    private String email;
    private String code;
}
