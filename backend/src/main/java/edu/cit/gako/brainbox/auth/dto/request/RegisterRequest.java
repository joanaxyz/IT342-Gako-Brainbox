package com.example.brainbox_api.auth.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String username, email, password;
}
