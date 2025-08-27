package com.myo.booking.repository;

import com.myo.booking.entity.Waitlist;
import com.myo.booking.entity.ClassSchedule;
import com.myo.booking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {
    List<Waitlist> findByClassScheduleAndStatusOrderByPositionAsc(ClassSchedule classSchedule, Waitlist.WaitlistStatus status);
    Optional<Waitlist> findByUserAndClassSchedule(User user, ClassSchedule classSchedule);
    
    @Query("SELECT MAX(w.position) FROM Waitlist w WHERE w.classSchedule = :classSchedule")
    Integer findMaxPosition(@Param("classSchedule") ClassSchedule classSchedule);
    
    List<Waitlist> findByClassScheduleAndStatus(ClassSchedule classSchedule, Waitlist.WaitlistStatus status);
}