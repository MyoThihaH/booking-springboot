package com.myo.booking.service;

import com.myo.booking.dto.request.BookingRequest;
import com.myo.booking.dto.response.BookingResponse;
import com.myo.booking.dto.response.ClassScheduleResponse;
import com.myo.booking.dto.response.MessageResponse;
import com.myo.booking.exception.BusinessException;
import org.springframework.http.HttpStatus;
import com.myo.booking.entity.*;
import com.myo.booking.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BookingService {
    
    private static final String BOOKING_LOCK_KEY = "booking:lock:";
    private static final long LOCK_TIMEOUT_SECONDS = 10;
    
    private final BookingRepository bookingRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final UserRepository userRepository;
    private final UserPackageRepository userPackageRepository;
    private final WaitlistRepository waitlistRepository;
    private final CountryRepository countryRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    public List<ClassScheduleResponse> getClassSchedules(String countryCode) {
        log.debug(countryCode);
        Country country = countryRepository.findByCode(countryCode)
            .orElseThrow(() -> new RuntimeException("Country not found"));
        
        List<ClassSchedule> schedules = classScheduleRepository
            .findUpcomingClasses(country, LocalDateTime.now());
        
        return schedules.stream()
            .map(this::convertToClassScheduleResponse)
            .collect(Collectors.toList());
    }
    
    public String bookClass(String email, BookingRequest request) {
        String lockKey = BOOKING_LOCK_KEY + request.getClassScheduleId();
        Boolean lockAcquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, email, LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        
        if (Boolean.FALSE.equals(lockAcquired)) {
            throw new BusinessException("Another booking is in progress. Please try again.", HttpStatus.TOO_MANY_REQUESTS);
        }
        
        try {
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
            
            ClassSchedule classSchedule = classScheduleRepository.findById(request.getClassScheduleId())
                .orElseThrow(() -> new BusinessException("Class not found", HttpStatus.NOT_FOUND));
            
            UserPackage userPackage = userPackageRepository.findById(request.getUserPackageId())
                .orElseThrow(() -> new BusinessException("User package not found", HttpStatus.NOT_FOUND));
            
            if (!userPackage.getUser().getId().equals(user.getId())) {
                throw new BusinessException("Invalid package", HttpStatus.FORBIDDEN);
            }
            
            if (!userPackage.getPackageEntity().getCountry().getId()
                    .equals(classSchedule.getCountry().getId())) {
                throw new BusinessException("Package country doesn't match class country", HttpStatus.BAD_REQUEST);
            }
            
            if (bookingRepository.findByUserAndClassSchedule(user, classSchedule).isPresent()) {
                throw new BusinessException("Already booked for this class", HttpStatus.CONFLICT);
            }
            
            List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                user, classSchedule.getStartTime(), classSchedule.getEndTime()
            );
            if (!overlapping.isEmpty()) {
                throw new BusinessException("You have another class at this time", HttpStatus.CONFLICT);
            }
            
            if (userPackage.getIsExpired() || userPackage.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new BusinessException("Package has expired", HttpStatus.BAD_REQUEST);
            }
            
            if (userPackage.getRemainingCredits() < classSchedule.getRequiredCredits()) {
                throw new BusinessException("Insufficient credits", HttpStatus.BAD_REQUEST);
            }
            
            Integer bookedSlots = bookingRepository.countBookedSlots(classSchedule);
            if (bookedSlots >= classSchedule.getMaxSlots()) {
                return addToWaitlist(user, classSchedule, userPackage);
            }
            
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setClassSchedule(classSchedule);
            booking.setUserPackage(userPackage);
            booking.setStatus(Booking.BookingStatus.BOOKED);
            booking.setCreditsUsed(classSchedule.getRequiredCredits());
            booking.setBookedAt(LocalDateTime.now());
            
            userPackage.setRemainingCredits(
                userPackage.getRemainingCredits() - classSchedule.getRequiredCredits()
            );
            
            classSchedule.setBookedSlots(bookedSlots + 1);
            
            bookingRepository.save(booking);
            userPackageRepository.save(userPackage);
            classScheduleRepository.save(classSchedule);

            return "Class booked successfully";
            
        } finally {
            redisTemplate.delete(lockKey);
        }
    }
    
    private String addToWaitlist(User user, ClassSchedule classSchedule, UserPackage userPackage) {
        if (waitlistRepository.findByUserAndClassSchedule(user, classSchedule).isPresent()) {
            throw new BusinessException("Already in waitlist for this class", HttpStatus.CONFLICT);
        }
        
        Integer maxPosition = waitlistRepository.findMaxPosition(classSchedule);
        int nextPosition = (maxPosition == null) ? 1 : maxPosition + 1;
        
        Waitlist waitlist = new Waitlist();
        waitlist.setUser(user);
        waitlist.setClassSchedule(classSchedule);
        waitlist.setUserPackage(userPackage);
        waitlist.setPosition(nextPosition);
        waitlist.setStatus(Waitlist.WaitlistStatus.WAITING);
        waitlist.setAddedAt(LocalDateTime.now());
        
        waitlistRepository.save(waitlist);

        return "Added user "+user.getEmail()+" to waitlist at position "+nextPosition+" for class "+classSchedule.getId();
    }
    
    public void cancelBooking(String email, Long bookingId) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
        
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new BusinessException("Booking not found", HttpStatus.NOT_FOUND));
        
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Unauthorized", HttpStatus.FORBIDDEN);
        }
        
        if (booking.getStatus() != Booking.BookingStatus.BOOKED) {
            throw new BusinessException("Cannot cancel this booking", HttpStatus.BAD_REQUEST);
        }
        
        LocalDateTime classStartTime = booking.getClassSchedule().getStartTime();
        LocalDateTime now = LocalDateTime.now();
        Duration timeDifference = Duration.between(now, classStartTime);
        
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setCancelledAt(now);
        
        // Refund credits if cancelled 4+ hours before
        if (timeDifference.toHours() >= 4) {
            UserPackage userPackage = booking.getUserPackage();
            userPackage.setRemainingCredits(
                userPackage.getRemainingCredits() + booking.getCreditsUsed()
            );
            userPackageRepository.save(userPackage);
        }
        
        ClassSchedule classSchedule = booking.getClassSchedule();
        classSchedule.setBookedSlots(classSchedule.getBookedSlots() - 1);
        classScheduleRepository.save(classSchedule);
        
        bookingRepository.save(booking);
        
        processWaitlist(classSchedule);
    }
    
    private void processWaitlist(ClassSchedule classSchedule) {
        List<Waitlist> waitlists = waitlistRepository
            .findByClassScheduleAndStatusOrderByPositionAsc(
                classSchedule, Waitlist.WaitlistStatus.WAITING
            );
        
        if (waitlists.isEmpty()) {
            return;
        }
        
        Waitlist firstWaitlist = waitlists.get(0);
        User user = firstWaitlist.getUser();
        UserPackage userPackage = firstWaitlist.getUserPackage();
        
        if (!userPackage.getIsExpired() && 
            userPackage.getRemainingCredits() >= classSchedule.getRequiredCredits()) {
            
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setClassSchedule(classSchedule);
            booking.setUserPackage(userPackage);
            booking.setStatus(Booking.BookingStatus.BOOKED);
            booking.setCreditsUsed(classSchedule.getRequiredCredits());
            booking.setBookedAt(LocalDateTime.now());
            
            userPackage.setRemainingCredits(
                userPackage.getRemainingCredits() - classSchedule.getRequiredCredits()
            );
            
            firstWaitlist.setStatus(Waitlist.WaitlistStatus.CONVERTED_TO_BOOKING);
            firstWaitlist.setConvertedToBookingAt(LocalDateTime.now());
            
            classSchedule.setBookedSlots(classSchedule.getBookedSlots() + 1);
            
            bookingRepository.save(booking);
            userPackageRepository.save(userPackage);
            waitlistRepository.save(firstWaitlist);
            classScheduleRepository.save(classSchedule);
        }
    }
    
    public void checkIn(String email, Long bookingId) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
        
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new BusinessException("Booking not found", HttpStatus.NOT_FOUND));
        
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Unauthorized", HttpStatus.FORBIDDEN);
        }
        
        if (booking.getStatus() != Booking.BookingStatus.BOOKED) {
            throw new BusinessException("Cannot check in for this booking", HttpStatus.BAD_REQUEST);
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime classStartTime = booking.getClassSchedule().getStartTime();
        
        // Allow check-in 15 minutes before and up to 10 minutes after class start
        if (now.isBefore(classStartTime.minusMinutes(15))) {
            throw new BusinessException("Check-in not yet available", HttpStatus.BAD_REQUEST);
        }
        
        if (now.isAfter(classStartTime.plusMinutes(10))) {
            throw new BusinessException("Check-in window has passed", HttpStatus.BAD_REQUEST);
        }
        
        booking.setStatus(Booking.BookingStatus.CHECKED_IN);
        booking.setCheckedInAt(now);
        bookingRepository.save(booking);
    }
    
    public List<BookingResponse> getUserBookings(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
        
        List<Booking> bookings = bookingRepository.findUpcomingBookings(user, LocalDateTime.now());
        
        return bookings.stream()
            .map(this::convertToBookingResponse)
            .collect(Collectors.toList());
    }
    
    private ClassScheduleResponse convertToClassScheduleResponse(ClassSchedule schedule) {
        ClassScheduleResponse response = new ClassScheduleResponse();
        response.setId(schedule.getId());
        response.setClassName(schedule.getClassName());
        response.setDescription(schedule.getDescription());
        response.setRequiredCredits(schedule.getRequiredCredits());
        response.setMaxSlots(schedule.getMaxSlots());
        response.setAvailableSlots(schedule.getMaxSlots() - schedule.getBookedSlots());
        response.setStartTime(schedule.getStartTime());
        response.setEndTime(schedule.getEndTime());
        response.setCountryCode(schedule.getCountry().getCode());
        response.setCountryName(schedule.getCountry().getName());
        response.setIsFull(schedule.getBookedSlots() >= schedule.getMaxSlots());
        return response;
    }
    
    private BookingResponse convertToBookingResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setUserId(booking.getUser().getId());
        response.setUserName(booking.getUser().getFirstName() + " " + booking.getUser().getLastName());
        response.setClassScheduleId(booking.getClassSchedule().getId());
        response.setClassName(booking.getClassSchedule().getClassName());
        response.setClassStartTime(booking.getClassSchedule().getStartTime());
        response.setClassEndTime(booking.getClassSchedule().getEndTime());
        response.setStatus(booking.getStatus());
        response.setBookedAt(booking.getBookedAt());
        response.setCancelledAt(booking.getCancelledAt());
        response.setCheckedInAt(booking.getCheckedInAt());
        response.setCreditsUsed(booking.getCreditsUsed());
        response.setCountryCode(booking.getClassSchedule().getCountry().getCode());
        return response;
    }
}