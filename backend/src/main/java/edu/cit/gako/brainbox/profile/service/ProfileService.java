package edu.cit.gako.brainbox.profile.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import edu.cit.gako.brainbox.auth.entity.User;
import edu.cit.gako.brainbox.auth.service.UserService;
import edu.cit.gako.brainbox.profile.dto.request.ChangePasswordRequest;
import edu.cit.gako.brainbox.profile.dto.request.UpdateProfileRequest;
import edu.cit.gako.brainbox.profile.dto.response.ProfileResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public ProfileResponse getProfile(Long userId) {
        User user = userService.findById(userId);
        return toResponse(user);
    }

    public ProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userService.findById(userId);

        if (request.getUsername() != null && !request.getUsername().isBlank()
                && !request.getUsername().equals(user.getUsername())) {
            if (userService.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Username already exists");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equals(user.getEmail())) {
            if (userService.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        User updated = userService.update(userId, user);
        return toResponse(updated);
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userService.findById(userId);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        userService.updatePassword(userId, passwordEncoder.encode(request.getNewPassword()));
    }

    private ProfileResponse toResponse(User user) {
        ProfileResponse response = new ProfileResponse();
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
