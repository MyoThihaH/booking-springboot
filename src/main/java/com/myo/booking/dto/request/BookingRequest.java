package com.myo.booking.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequest {
    @NotNull(message = "Class schedule ID is required")
    private Long classScheduleId;
    
    @NotNull(message = "User package ID is required")
    private Long userPackageId;
}