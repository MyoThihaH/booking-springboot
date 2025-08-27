package com.myo.booking.init;

import com.myo.booking.entity.ClassSchedule;
import com.myo.booking.entity.Country;
import com.myo.booking.entity.Package;
import com.myo.booking.repository.ClassScheduleRepository;
import com.myo.booking.repository.CountryRepository;
import com.myo.booking.repository.PackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private CountryRepository countryRepository;
    
    @Autowired
    private PackageRepository packageRepository;
    
    @Autowired
    private ClassScheduleRepository classScheduleRepository;
    
    @Override
    public void run(String... args) {
        if (countryRepository.count() == 0) {
            initializeData();
        }
    }
    
    private void initializeData() {
        Country singapore = new Country();
        singapore.setCode("SG");
        singapore.setName("Singapore");
        countryRepository.save(singapore);
        
        Country myanmar = new Country();
        myanmar.setCode("MM");
        myanmar.setName("Myanmar");
        countryRepository.save(myanmar);
        
        Country thailand = new Country();
        thailand.setCode("TH");
        thailand.setName("Thailand");
        countryRepository.save(thailand);
        
        Package basicSG = new Package();
        basicSG.setName("Basic Package SG");
        basicSG.setCredits(5);
        basicSG.setPrice(new BigDecimal("50.00"));
        basicSG.setValidityDays(30);
        basicSG.setCountry(singapore);
        basicSG.setActive(true);
        packageRepository.save(basicSG);
        
        Package premiumSG = new Package();
        premiumSG.setName("Premium Package SG");
        premiumSG.setCredits(15);
        premiumSG.setPrice(new BigDecimal("120.00"));
        premiumSG.setValidityDays(60);
        premiumSG.setCountry(singapore);
        premiumSG.setActive(true);
        packageRepository.save(premiumSG);
        
        Package basicMM = new Package();
        basicMM.setName("Basic Package MM");
        basicMM.setCredits(5);
        basicMM.setPrice(new BigDecimal("30.00"));
        basicMM.setValidityDays(30);
        basicMM.setCountry(myanmar);
        basicMM.setActive(true);
        packageRepository.save(basicMM);
        
        Package basicTH = new Package();
        basicTH.setName("Basic Package TH");
        basicTH.setCredits(5);
        basicTH.setPrice(new BigDecimal("40.00"));
        basicTH.setValidityDays(30);
        basicTH.setCountry(thailand);
        basicTH.setActive(true);
        packageRepository.save(basicTH);
        
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
        
        ClassSchedule yogaSG = new ClassSchedule();
        yogaSG.setClassName("1 hr Yoga Class");
        yogaSG.setDescription("Morning yoga session for all levels");
        yogaSG.setRequiredCredits(1);
        yogaSG.setMaxSlots(5);
        yogaSG.setBookedSlots(0);
        yogaSG.setStartTime(tomorrow);
        yogaSG.setEndTime(tomorrow.plusHours(1));
        yogaSG.setCountry(singapore);
        yogaSG.setIsCompleted(false);
        classScheduleRepository.save(yogaSG);
        
        ClassSchedule pilatesSG = new ClassSchedule();
        pilatesSG.setClassName("Pilates Class");
        pilatesSG.setDescription("Core strengthening pilates");
        pilatesSG.setRequiredCredits(2);
        pilatesSG.setMaxSlots(5);
        pilatesSG.setBookedSlots(0);
        pilatesSG.setStartTime(tomorrow.plusHours(2));
        pilatesSG.setEndTime(tomorrow.plusHours(3));
        pilatesSG.setCountry(singapore);
        pilatesSG.setIsCompleted(false);
        classScheduleRepository.save(pilatesSG);
        
        ClassSchedule yogaMM = new ClassSchedule();
        yogaMM.setClassName("1 hr Yoga Class");
        yogaMM.setDescription("Evening yoga session");
        yogaMM.setRequiredCredits(1);
        yogaMM.setMaxSlots(5);
        yogaMM.setBookedSlots(0);
        yogaMM.setStartTime(tomorrow.withHour(18));
        yogaMM.setEndTime(tomorrow.withHour(19));
        yogaMM.setCountry(myanmar);
        yogaMM.setIsCompleted(false);
        classScheduleRepository.save(yogaMM);
        
        ClassSchedule muayThaiTH = new ClassSchedule();
        muayThaiTH.setClassName("Muay Thai Class");
        muayThaiTH.setDescription("Traditional Muay Thai training");
        muayThaiTH.setRequiredCredits(3);
        muayThaiTH.setMaxSlots(5);
        muayThaiTH.setBookedSlots(0);
        muayThaiTH.setStartTime(tomorrow.withHour(16));
        muayThaiTH.setEndTime(tomorrow.withHour(18));
        muayThaiTH.setCountry(thailand);
        muayThaiTH.setIsCompleted(false);
        classScheduleRepository.save(muayThaiTH);
        
        System.out.println("Sample data initialized successfully!");
    }
}