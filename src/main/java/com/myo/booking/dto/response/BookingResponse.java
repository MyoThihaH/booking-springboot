package com.myo.booking.dto.response;

import com.myo.booking.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long classScheduleId;
    private String className;
    private LocalDateTime classStartTime;
    private LocalDateTime classEndTime;
    private Booking.BookingStatus status;
    private LocalDateTime bookedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime checkedInAt;
    private Integer creditsUsed;
    private String countryCode;
}