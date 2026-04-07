package edu.cit.gako.brainbox.profile.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.gako.brainbox.auth.annotation.RequireAuth;
import edu.cit.gako.brainbox.common.dto.ApiResponse;
import edu.cit.gako.brainbox.profile.dto.request.ChangePasswordRequest;
import edu.cit.gako.brainbox.profile.dto.request.UpdateProfileRequest;
import edu.cit.gako.brainbox.profile.dto.response.ProfileResponse;
import edu.cit.gako.brainbox.profile.service.ProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @RequireAuth
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(@RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(profileService.getProfile(userId)));
    }

    @RequireAuth
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @RequestAttribute Long userId,
            @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(profileService.updateProfile(userId, request)));
    }

    @RequireAuth
    @PostMapping("/me/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestAttribute Long userId,
            @RequestBody ChangePasswordRequest request) {
        profileService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
