package edu.cit.gako.brainbox.modules.user.controller;

import edu.cit.gako.brainbox.modules.user.dto.request.ChangePasswordRequest;
import edu.cit.gako.brainbox.modules.user.dto.request.UpdateProfileRequest;
import edu.cit.gako.brainbox.modules.user.dto.response.ProfileResponse;
import edu.cit.gako.brainbox.modules.user.service.ProfileService;
import edu.cit.gako.brainbox.platform.security.annotation.RequireAuth;
import edu.cit.gako.brainbox.shared.controller.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @RequireAuth
    @GetMapping({"/api/user/me", "/api/users/me"})
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(@RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(profileService.getProfile(userId)));
    }

    @RequireAuth
    @PutMapping({"/api/user/me", "/api/users/me"})
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @RequestAttribute Long userId,
            @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(profileService.updateProfile(userId, request)));
    }

    @RequireAuth
    @PostMapping({"/api/user/me/change-password", "/api/users/me/password"})
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestAttribute Long userId,
            @RequestBody ChangePasswordRequest request) {
        profileService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
