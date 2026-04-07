package edu.cit.gako.brainbox.auth.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    String accessToken, refreshToken;
}
