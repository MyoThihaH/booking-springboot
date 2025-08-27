package com.myo.booking.repository;

import com.myo.booking.entity.UserPackage;
import com.myo.booking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserPackageRepository extends JpaRepository<UserPackage, Long> {
    List<UserPackage> findByUser(User user);
    
    @Query("SELECT up FROM UserPackage up WHERE up.user = :user AND up.isExpired = false " +
           "AND up.remainingCredits > 0 AND up.packageEntity.country.id = :countryId " +
           "ORDER BY up.expiresAt ASC")
    List<UserPackage> findActivePackagesByUserAndCountry(@Param("user") User user, @Param("countryId") Long countryId);
    
    @Query("SELECT up FROM UserPackage up WHERE up.expiresAt < :now AND up.isExpired = false")
    List<UserPackage> findExpiredPackages(@Param("now") LocalDateTime now);
}