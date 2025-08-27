package com.myo.booking.job;

import com.myo.booking.entity.Booking;
import com.myo.booking.entity.ClassSchedule;
import com.myo.booking.entity.UserPackage;
import com.myo.booking.entity.Waitlist;
import com.myo.booking.repository.BookingRepository;
import com.myo.booking.repository.ClassScheduleRepository;
import com.myo.booking.repository.UserPackageRepository;
import com.myo.booking.repository.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClassCompletionJob implements Job {
    
    private final ClassScheduleRepository classScheduleRepository;
    private final BookingRepository bookingRepository;
    private final WaitlistRepository waitlistRepository;
    private final UserPackageRepository userPackageRepository;
    
    @Override
    @Transactional
    public void execute(JobExecutionContext context) {
        processCompletedClasses();
    }
    
    public void processCompletedClasses() {
        log.info("Class complete job trigger...");
        LocalDateTime now = LocalDateTime.now();
        List<ClassSchedule> completedClasses = classScheduleRepository.findClassesToComplete(now);
        
        for (ClassSchedule classSchedule : completedClasses) {
            classSchedule.setIsCompleted(true);
            classScheduleRepository.save(classSchedule);
            
            List<Booking> bookings = bookingRepository.findByClassSchedule(classSchedule);
            for (Booking booking : bookings) {
                if (booking.getStatus() == Booking.BookingStatus.CHECKED_IN) {
                    booking.setStatus(Booking.BookingStatus.COMPLETED);
                }
                bookingRepository.save(booking);
            }
            
            List<Waitlist> waitlists = waitlistRepository.findByClassScheduleAndStatus(
                classSchedule, Waitlist.WaitlistStatus.WAITING
            );
            
            for (Waitlist waitlist : waitlists) {
                UserPackage userPackage = waitlist.getUserPackage();
                userPackage.setRemainingCredits(
                        userPackage.getRemainingCredits() + classSchedule.getRequiredCredits()
                );
                userPackageRepository.save(userPackage);

                waitlist.setStatus(Waitlist.WaitlistStatus.REFUNDED);
                waitlist.setRefundedAt(now);
                waitlistRepository.save(waitlist);
            }
        }
    }
}