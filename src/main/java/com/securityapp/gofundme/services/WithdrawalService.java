/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.services;

import com.securityapp.gofundme.dto.CreatorBalance;
import com.securityapp.gofundme.model.*;
import com.securityapp.gofundme.repositories.WithdrawalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WithdrawalService {

    @Autowired
    private WithdrawalRepository withdrawalRepository;

    @Autowired
    private FinancialService financialService;

    @Value("${withdrawal.min.amount:10.00}")
    private BigDecimal minWithdrawalAmount;

    /**
     * Calcule le solde réellement disponible = gains - retraits déjà demandés
     */
    public BigDecimal getAvailableBalance(User user) {
        CreatorBalance balance = financialService.calculateBalance(user);
        BigDecimal alreadyWithdrawn = withdrawalRepository.sumWithdrawnByUser(user);
        return balance.getAvailable().subtract(alreadyWithdrawn);
    }

    @Transactional
    public Withdrawal requestWithdrawal(User user, BigDecimal amount, PaymentMethod method,
                                        String recipientPhone, String bankName,
                                        String bankAccountNumber, String bankAccountName) {
        
        // Vérifications
        if (amount == null || amount.compareTo(minWithdrawalAmount) < 0) {
            throw new RuntimeException("Le montant minimum de retrait est de " + minWithdrawalAmount + " $");
        }

        BigDecimal available = getAvailableBalance(user);
        if (amount.compareTo(available) > 0) {
            throw new RuntimeException("Solde insuffisant. Disponible : " + available + " $");
        }

        // Vérifier qu'il n'a pas déjà une demande en cours
        if (withdrawalRepository.existsByUserAndStatus(user, WithdrawalStatus.PENDING)) {
            throw new RuntimeException("Vous avez déjà une demande de retrait en attente.");
        }

        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setUser(user);
        withdrawal.setAmount(amount);
        withdrawal.setMethod(method);
        withdrawal.setStatus(WithdrawalStatus.PENDING);

        if (method == PaymentMethod.MONCASH) {
            if (recipientPhone == null || recipientPhone.isBlank()) {
                throw new RuntimeException("Le numéro de téléphone est requis pour MonCash.");
            }
            withdrawal.setRecipientPhone(recipientPhone);
        } else {
            if (bankName == null || bankName.isBlank() ||
                bankAccountNumber == null || bankAccountNumber.isBlank() ||
                bankAccountName == null || bankAccountName.isBlank()) {
                throw new RuntimeException("Les informations bancaires sont incomplètes.");
            }
            withdrawal.setBankName(bankName);
            withdrawal.setBankAccountNumber(bankAccountNumber);
            withdrawal.setBankAccountName(bankAccountName);
        }

        return withdrawalRepository.save(withdrawal);
    }

    public List<Withdrawal> getUserWithdrawals(User user) {
        return withdrawalRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Withdrawal> getPendingWithdrawals() {
        return withdrawalRepository.findByStatusOrderByCreatedAtDesc(WithdrawalStatus.PENDING);
    }

    @Transactional
    public void approveWithdrawal(Long withdrawalId) {
        Withdrawal w = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));
        
        if (w.getStatus() != WithdrawalStatus.PENDING) {
            throw new RuntimeException("Cette demande a déjà été traitée.");
        }

        w.setStatus(WithdrawalStatus.COMPLETED);
        w.setProcessedAt(LocalDateTime.now());
        withdrawalRepository.save(w);
    }

    @Transactional
    public void rejectWithdrawal(Long withdrawalId, String reason) {
        Withdrawal w = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));
        
        if (w.getStatus() != WithdrawalStatus.PENDING) {
            throw new RuntimeException("Cette demande a déjà été traitée.");
        }

        w.setStatus(WithdrawalStatus.REJECTED);
        w.setRejectionReason(reason);
        w.setProcessedAt(LocalDateTime.now());
        withdrawalRepository.save(w);
    }
}