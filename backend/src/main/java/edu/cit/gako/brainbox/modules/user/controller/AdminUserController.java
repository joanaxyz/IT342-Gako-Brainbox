package edu.cit.gako.brainbox.modules.user.controller;

import edu.cit.gako.brainbox.modules.user.dto.request.UserAdminUpdateRequest;
import edu.cit.gako.brainbox.modules.user.dto.response.UserAdminResponse;
import edu.cit.gako.brainbox.modules.user.service.UserService;
import edu.cit.gako.brainbox.modules.user.entity.User;
import edu.cit.gako.brainbox.modules.user.entity.UserRole;
import edu.cit.gako.brainbox.platform.security.annotation.RequireRole;
import edu.cit.gako.brainbox.shared.controller.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @RequireRole(UserRole.ADMIN)
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserAdminResponse>>> getUsers() {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getAllUsers().stream()
                        .map(this::mapToResponse)
                        .toList()));
    }

    @RequireRole(UserRole.ADMIN)
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserAdminResponse>> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(userService.findById(userId))));
    }

    @RequireRole(UserRole.ADMIN)
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserAdminResponse>> updateUser(
            @PathVariable Long userId,
            @RequestBody UserAdminUpdateRequest request) {
        User user = userService.findById(userId);
        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getBanned() != null) {
            user.setBanned(request.getBanned());
        }
        if (request.getVerified() != null) {
            user.setVerified(request.getVerified());
        }
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(userService.save(user))));
    }

    @RequireRole(UserRole.ADMIN)
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    private UserAdminResponse mapToResponse(User user) {
        UserAdminResponse response = new UserAdminResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setBanned(user.isBanned());
        response.setVerified(user.isVerified());
        response.setAuthProvider(user.getAuthProvider());
        response.setCreatedAt(user.getCreatedAt());
        response.setLastLogin(user.getLastLogin());
        response.setLastLogout(user.getLastLogout());
        return response;
    }
}
