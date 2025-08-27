package com.myo.booking.config;

import com.myo.booking.job.ClassCompletionJob;
import com.myo.booking.job.PackageExpiryJob;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {
    
    @Value("${job.class-completion.interval-minutes}")
    private int classCompletionIntervalMinutes;
    
    @Value("${job.package-expiry.cron}")
    private String packageExpiryCron;
    
    @Bean
    public JobDetail classCompletionJobDetail() {
        return JobBuilder.newJob(ClassCompletionJob.class)
                .withIdentity("classCompletionJob")
                .storeDurably()
                .build();
    }
    
    @Bean
    public Trigger classCompletionTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(classCompletionJobDetail())
                .withIdentity("classCompletionTrigger")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(classCompletionIntervalMinutes)
                        .repeatForever())
                .build();
    }
    
    @Bean
    public JobDetail packageExpiryJobDetail() {
        return JobBuilder.newJob(PackageExpiryJob.class)
                .withIdentity("packageExpiryJob")
                .storeDurably()
                .build();
    }
    
    @Bean
    public Trigger packageExpiryTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(packageExpiryJobDetail())
                .withIdentity("packageExpiryTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(packageExpiryCron))
                .build();
    }
}