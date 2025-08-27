package com.myo.booking.service;

import com.myo.booking.dto.request.PurchasePackageRequest;
import com.myo.booking.dto.response.MessageResponse;
import com.myo.booking.exception.BusinessException;
import org.springframework.http.HttpStatus;
import com.myo.booking.dto.response.PackageResponse;
import com.myo.booking.dto.response.UserPackageResponse;
import com.myo.booking.entity.Package;
import com.myo.booking.entity.User;
import com.myo.booking.entity.UserPackage;
import com.myo.booking.entity.Country;
import com.myo.booking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PackageService {
    
    private final PackageRepository packageRepository;
    private final UserPackageRepository userPackageRepository;
    private final UserRepository userRepository;
    private final CountryRepository countryRepository;
    private final PaymentService paymentService;
    
    public List<PackageResponse> getAvailablePackages(String countryCode) {
        List<Package> packages;
        
        if (countryCode != null) {
            Country country = countryRepository.findByCode(countryCode)
                .orElseThrow(() -> new BusinessException("Country not found", HttpStatus.NOT_FOUND));
            packages = packageRepository.findByCountryAndActiveTrue(country);
        } else {
            packages = packageRepository.findByActiveTrue();
        }
        
        return packages.stream()
            .map(this::convertToPackageResponse)
            .collect(Collectors.toList());
    }
    
    public List<UserPackageResponse> getUserPackages(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
        
        List<UserPackage> userPackages = userPackageRepository.findByUser(user);
        
        userPackages.forEach(up -> {
            if (!up.getIsExpired() && up.getExpiresAt().isBefore(LocalDateTime.now())) {
                up.setIsExpired(true);
                userPackageRepository.save(up);
            }
        });
        
        return userPackages.stream()
            .map(this::convertToUserPackageResponse)
            .collect(Collectors.toList());
    }
    
    public void purchasePackage(String email, PurchasePackageRequest request) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
        
        Package packageEntity = packageRepository.findById(request.getPackageId())
            .orElseThrow(() -> new BusinessException("Package not found", HttpStatus.NOT_FOUND));
        
        boolean paymentAdded = paymentService.addPaymentCard(
            request.getCardNumber(),
            request.getCardHolderName()
        );
        
        if (!paymentAdded) {
            throw new BusinessException("Failed to add payment method", HttpStatus.BAD_REQUEST);
        }
        
        boolean paymentCharged = paymentService.chargePayment(
            request.getCardNumber(),
            packageEntity.getPrice(),
            "Package: " + packageEntity.getName()
        );
        
        if (!paymentCharged) {
            throw new BusinessException("Payment failed", HttpStatus.PAYMENT_REQUIRED);
        }
        
        UserPackage userPackage = new UserPackage();
        userPackage.setUser(user);
        userPackage.setPackageEntity(packageEntity);
        userPackage.setRemainingCredits(packageEntity.getCredits());
        userPackage.setPurchasedAt(LocalDateTime.now());
        userPackage.setExpiresAt(LocalDateTime.now().plusDays(packageEntity.getValidityDays()));
        userPackage.setIsExpired(false);
        userPackage.setPaymentTransactionId(paymentService.generateTransactionId());
        
        userPackageRepository.save(userPackage);
    }
    
    private PackageResponse convertToPackageResponse(Package pkg) {
        PackageResponse response = new PackageResponse();
        response.setId(pkg.getId());
        response.setName(pkg.getName());
        response.setCredits(pkg.getCredits());
        response.setPrice(pkg.getPrice());
        response.setValidityDays(pkg.getValidityDays());
        response.setCountryCode(pkg.getCountry().getCode());
        response.setCountryName(pkg.getCountry().getName());
        return response;
    }
    
    private UserPackageResponse convertToUserPackageResponse(UserPackage userPackage) {
        UserPackageResponse response = new UserPackageResponse();
        response.setId(userPackage.getId());
        response.setPackageName(userPackage.getPackageEntity().getName());
        response.setRemainingCredits(userPackage.getRemainingCredits());
        response.setPurchasedAt(userPackage.getPurchasedAt());
        response.setExpiresAt(userPackage.getExpiresAt());
        response.setIsExpired(userPackage.getIsExpired());
        response.setCountryCode(userPackage.getPackageEntity().getCountry().getCode());
        response.setCountryName(userPackage.getPackageEntity().getCountry().getName());
        return response;
    }
}