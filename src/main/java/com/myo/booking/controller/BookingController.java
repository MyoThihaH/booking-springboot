package com.myo.booking.controller;

import com.myo.booking.dto.request.BookingRequest;
import com.myo.booking.dto.response.BookingResponse;
import com.myo.booking.dto.response.ClassScheduleResponse;
import com.myo.booking.dto.response.MessageResponse;
import com.myo.booking.dto.response.StandardResponse;
import com.myo.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@Tag(name = "Booking Management", description = "Class schedule and booking endpoints")
@RequiredArgsConstructor
public class BookingController {
    
    private final BookingService bookingService;
    
    @GetMapping("/schedules/country/{countryCode}")
    @Operation(summary = "Get class schedules by country")
    public ResponseEntity<StandardResponse<List<ClassScheduleResponse>>> getClassSchedules(
            @PathVariable String countryCode) {
        log.debug("hit schedule api");
        List<ClassScheduleResponse> schedules = bookingService.getClassSchedules(countryCode);
        return ResponseEntity.ok(StandardResponse.success("Schedules retrieved successfully", schedules));
    }
    
    @PostMapping("/bookings")
    @Operation(summary = "Book a class")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<StandardResponse<?>> bookClass(
            Authentication authentication,
            @Valid @RequestBody BookingRequest request) {

        return ResponseEntity.ok(StandardResponse.success(bookingService.bookClass(authentication.getName(), request)));
    }
    
    @DeleteMapping("/bookings/{bookingId}")
    @Operation(summary = "Cancel a booking")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<StandardResponse<?>> cancelBooking(
            Authentication authentication,
            @PathVariable Long bookingId) {
        bookingService.cancelBooking(authentication.getName(), bookingId);
        return ResponseEntity.ok(StandardResponse.success("Booking cancelled successfully"));
    }
    
    @PostMapping("/bookings/{bookingId}/check-in")
    @Operation(summary = "Check in to a class")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<StandardResponse<?>> checkIn(
            Authentication authentication,
            @PathVariable Long bookingId) {
        bookingService.checkIn(authentication.getName(), bookingId);
        return ResponseEntity.ok(StandardResponse.success("Checked in successfully"));
    }
    
    @GetMapping("/bookings/my-bookings")
    @Operation(summary = "Get user's bookings")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<StandardResponse<List<BookingResponse>>> getUserBookings(Authentication authentication) {
        List<BookingResponse> bookings = bookingService.getUserBookings(authentication.getName());
        return ResponseEntity.ok(StandardResponse.success("Bookings retrieved successfully", bookings));
    }
}