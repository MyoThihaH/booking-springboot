package com.myo.booking.job;

import com.myo.booking.entity.UserPackage;
import com.myo.booking.repository.UserPackageRepository;
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
public class PackageExpiryJob implements Job {
    
    private final UserPackageRepository userPackageRepository;
    
    @Override
    @Transactional
    public void execute(JobExecutionContext context) {

        log.info("Package expire job trigger...");
        updateExpiredPackages();
    }
    
    public void updateExpiredPackages() {
        LocalDateTime now = LocalDateTime.now();
        List<UserPackage> expiredPackages = userPackageRepository.findExpiredPackages(now);
        
        for (UserPackage userPackage : expiredPackages) {
            userPackage.setIsExpired(true);
            userPackageRepository.save(userPackage);
        }
    }
}