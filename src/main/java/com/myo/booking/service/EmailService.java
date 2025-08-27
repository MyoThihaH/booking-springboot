package com.myo.booking.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
    

    public boolean sendVerificationEmail(String email, String token) {
        System.out.println("Sending verification email to: " + email);
        System.out.println("Verification token: " + token);
        return true;
    }
    
    public boolean sendPasswordResetEmail(String email, String token) {
        System.out.println("Sending password reset email to: " + email);
        System.out.println("Reset token: " + token);
        return true;
    }
}