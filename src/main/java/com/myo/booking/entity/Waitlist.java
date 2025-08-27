package com.myo.booking.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "waitlists")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Waitlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_schedule_id", nullable = false)
    private ClassSchedule classSchedule;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_package_id", nullable = false)
    private UserPackage userPackage;
    
    @Column(nullable = false)
    private LocalDateTime addedAt = LocalDateTime.now();
    
    @Column(nullable = false)
    private Integer position;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WaitlistStatus status = WaitlistStatus.WAITING;
    
    private LocalDateTime convertedToBookingAt;
    
    private LocalDateTime refundedAt;
    
    public enum WaitlistStatus {
        WAITING, CONVERTED_TO_BOOKING, REFUNDED, CANCELLED
    }
}