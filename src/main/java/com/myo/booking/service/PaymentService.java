package com.myo.booking.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentService {
    
    public boolean addPaymentCard(String cardNumber, String cardHolderName) {
        System.out.println("Adding payment card for: " + cardHolderName);
        return true;
    }
    
    public boolean chargePayment(String cardNumber, BigDecimal amount, String description) {
        System.out.println("Charging payment: " + amount + " for " + description);
        return true;
    }
    
    public String generateTransactionId() {
        return "TXN_" + System.currentTimeMillis();
    }
}