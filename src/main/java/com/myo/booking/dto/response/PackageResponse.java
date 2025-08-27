package com.myo.booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PackageResponse {
    private Long id;
    private String name;
    private Integer credits;
    private BigDecimal price;
    private Integer validityDays;
    private String countryCode;
    private String countryName;
}