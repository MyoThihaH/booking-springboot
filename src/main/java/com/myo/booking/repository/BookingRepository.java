package com.myo.booking.repository;

import com.myo.booking.entity.Booking;
import com.myo.booking.entity.ClassSchedule;
import com.myo.booking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);
    List<Booking> findByClassSchedule(ClassSchedule classSchedule);
    Optional<Booking> findByUserAndClassSchedule(User user, ClassSchedule classSchedule);
    
    @Query("SELECT b FROM Booking b WHERE b.user = :user AND b.status = 'BOOKED' " +
           "AND b.classSchedule.startTime > :now ORDER BY b.classSchedule.startTime ASC")
    List<Booking> findUpcomingBookings(@Param("user") User user, @Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.classSchedule = :classSchedule AND b.status = 'BOOKED'")
    Integer countBookedSlots(@Param("classSchedule") ClassSchedule classSchedule);
    
    @Query("SELECT b FROM Booking b WHERE b.user = :user AND b.status = 'BOOKED' " +
           "AND ((b.classSchedule.startTime <= :endTime AND b.classSchedule.endTime >= :startTime))")
    List<Booking> findOverlappingBookings(@Param("user") User user, 
                                         @Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);
}