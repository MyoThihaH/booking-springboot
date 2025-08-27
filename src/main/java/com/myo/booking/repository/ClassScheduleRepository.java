package com.myo.booking.repository;

import com.myo.booking.entity.ClassSchedule;
import com.myo.booking.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Long> {
    List<ClassSchedule> findByCountryAndStartTimeAfter(Country country, LocalDateTime afterTime);
    
    @Query("SELECT cs FROM ClassSchedule cs WHERE cs.endTime < :now AND cs.isCompleted = false")
    List<ClassSchedule> findClassesToComplete(@Param("now") LocalDateTime now);
    
    @Query("SELECT cs FROM ClassSchedule cs WHERE cs.country = :country " +
           "AND cs.startTime > :now AND cs.isCompleted = false ORDER BY cs.startTime ASC")
    List<ClassSchedule> findUpcomingClasses(@Param("country") Country country, @Param("now") LocalDateTime now);
}