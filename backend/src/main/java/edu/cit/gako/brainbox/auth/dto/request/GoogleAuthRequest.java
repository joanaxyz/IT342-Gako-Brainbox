package edu.cit.gako.brainbox.auth.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleAuthRequest {
    private String idToken;
    private String accessToken;
}
