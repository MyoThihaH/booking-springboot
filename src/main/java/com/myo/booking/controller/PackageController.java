package com.myo.booking.controller;

import com.myo.booking.dto.request.PurchasePackageRequest;
import com.myo.booking.dto.response.MessageResponse;
import com.myo.booking.dto.response.PackageResponse;
import com.myo.booking.dto.response.UserPackageResponse;
import com.myo.booking.dto.response.StandardResponse;
import com.myo.booking.service.PackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packages")
@Tag(name = "Package Management", description = "Package endpoints")
@RequiredArgsConstructor
public class PackageController {
    
    private final PackageService packageService;
    
    @GetMapping()
    @Operation(summary = "Get available packages")
    public ResponseEntity<StandardResponse<List<PackageResponse>>> getAvailablePackages(
            @RequestParam(required = false) String countryCode) {
        List<PackageResponse> packages = packageService.getAvailablePackages(countryCode);
        return ResponseEntity.ok(StandardResponse.success("Packages retrieved successfully", packages));
    }
    
    @GetMapping("/my-packages")
    @Operation(summary = "Get user's packages")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<StandardResponse<List<UserPackageResponse>>> getUserPackages(Authentication authentication) {
        List<UserPackageResponse> packages = packageService.getUserPackages(authentication.getName());
        return ResponseEntity.ok(StandardResponse.success("User packages retrieved successfully", packages));
    }
    
    @PostMapping("/purchase")
    @Operation(summary = "Purchase a package")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<StandardResponse<?>> purchasePackage(
            Authentication authentication,
            @Valid @RequestBody PurchasePackageRequest request) {
        packageService.purchasePackage(authentication.getName(), request);
        return ResponseEntity.ok(StandardResponse.success("Package purchased successfully"));
    }
}