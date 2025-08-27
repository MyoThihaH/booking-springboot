package com.myo.booking.repository;

import com.myo.booking.entity.Package;
import com.myo.booking.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {
    List<Package> findByCountryAndActiveTrue(Country country);
    List<Package> findByActiveTrue();
}