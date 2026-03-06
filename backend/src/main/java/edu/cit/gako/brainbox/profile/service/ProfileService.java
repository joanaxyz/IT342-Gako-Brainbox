package com.example.brainbox_api.profile.service;

import org.springframework.stereotype.Service;

import com.example.brainbox_api.auth.entity.User;
import com.example.brainbox_api.auth.service.UserService;
import com.example.brainbox_api.profile.dto.response.ProfileResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final UserService userService;

    public ProfileResponse getProfile(Long id){
        User user = userService.findById(id);
        ProfileResponse response = new ProfileResponse();
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        return response;
    }
}
