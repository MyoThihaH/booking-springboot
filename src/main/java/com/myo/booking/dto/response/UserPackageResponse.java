package com.myo.booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPackageResponse {
    private Long id;
    private String packageName;
    private Integer remainingCredits;
    private LocalDateTime purchasedAt;
    private LocalDateTime expiresAt;
    private Boolean isExpired;
    private String countryCode;
    private String countryName;
}