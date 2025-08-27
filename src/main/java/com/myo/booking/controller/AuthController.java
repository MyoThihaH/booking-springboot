package com.myo.booking.controller;

import com.myo.booking.dto.request.*;
import com.myo.booking.dto.response.AuthResponse;
import com.myo.booking.dto.response.MessageResponse;
import com.myo.booking.dto.response.StandardResponse;
import com.myo.booking.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    
    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<StandardResponse<?>> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return ResponseEntity.ok(StandardResponse.success("User registered successfully. Please check your email for verification."));
    }
    
    @PostMapping("/login")
    @Operation(summary = "User login")
    public ResponseEntity<StandardResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.debug("reach here");
        AuthResponse authResponse = userService.login(request);
        return ResponseEntity.ok(StandardResponse.success("Login successful", authResponse));
    }
    
    @GetMapping("/verify-email")
    @Operation(summary = "Verify email with token")
    public ResponseEntity<StandardResponse<?>> verifyEmail(@RequestParam String token) {
        userService.verifyEmail(token);
        return ResponseEntity.ok(StandardResponse.success("Email verified successfully"));
    }
    
    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset")
    public ResponseEntity<StandardResponse<?>> forgotPassword(@RequestParam String email) {
        userService.requestPasswordReset(email);
        return ResponseEntity.ok(StandardResponse.success("Password reset email sent"));
    }
    
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password with token")
    public ResponseEntity<StandardResponse<?>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.ok(StandardResponse.success("Password reset successfully"));
    }
}