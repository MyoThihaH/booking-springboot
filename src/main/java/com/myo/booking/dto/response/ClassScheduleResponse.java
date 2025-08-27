package com.myo.booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassScheduleResponse {
    private Long id;
    private String className;
    private String description;
    private Integer requiredCredits;
    private Integer maxSlots;
    private Integer availableSlots;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String countryCode;
    private String countryName;
    private Boolean isFull;
}