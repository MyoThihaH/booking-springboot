package com.myo.booking.controller;

import com.myo.booking.dto.request.ChangePasswordRequest;
import com.myo.booking.dto.response.MessageResponse;
import com.myo.booking.dto.response.UserResponse;
import com.myo.booking.dto.response.StandardResponse;
import com.myo.booking.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "User profile endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/profile")
    @Operation(summary = "Get user profile")
    public ResponseEntity<StandardResponse<UserResponse>> getProfile(Authentication authentication) {
        UserResponse profile = userService.getProfile(authentication.getName());
        return ResponseEntity.ok(StandardResponse.success("Profile retrieved successfully", profile));
    }
    
    @PostMapping("/change-password")
    @Operation(summary = "Change password")
    public ResponseEntity<StandardResponse<?>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(StandardResponse.success("Password changed successfully"));
    }
}