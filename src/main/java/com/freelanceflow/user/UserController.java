package com.freelanceflow.user;

import com.freelanceflow.common.ApiResponse;
import com.freelanceflow.common.SecurityUtils;
import com.freelanceflow.user.dto.UpdateProfileRequest;
import com.freelanceflow.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User profile management")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        UserResponse response = userService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @RequestBody UpdateProfileRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        UserResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
