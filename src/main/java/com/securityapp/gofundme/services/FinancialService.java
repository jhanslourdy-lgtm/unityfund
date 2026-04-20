package com.securityapp.gofundme.services;

import com.securityapp.gofundme.dto.CreatorBalance;
import com.securityapp.gofundme.model.Payment;
import com.securityapp.gofundme.model.PaymentStatus;
import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class FinancialService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    public CreatorBalance calculateBalance(User creator) {
        List<Payment> payments = paymentRepository.findByDonationCampaignUserAndStatus(creator, PaymentStatus.COMPLETED);
        
        BigDecimal totalRaised = payments.stream()
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal totalFees = payments.stream()
            .map(Payment::getPlatformFee)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal available = payments.stream()
            .map(Payment::getNetAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        return new CreatorBalance(totalRaised, totalFees, available);
    }
}