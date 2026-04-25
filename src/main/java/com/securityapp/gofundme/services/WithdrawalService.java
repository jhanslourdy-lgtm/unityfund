package com.securityapp.gofundme.services;

import com.securityapp.gofundme.dto.CreatorBalance;
import com.securityapp.gofundme.model.PaymentMethod;
import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.model.Withdrawal;
import com.securityapp.gofundme.model.WithdrawalStatus;
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

    public BigDecimal getMinWithdrawalAmount() {
        return minWithdrawalAmount;
    }

    public BigDecimal getAvailableBalance(User user) {
        CreatorBalance balance = financialService.calculateBalance(user);
        BigDecimal alreadyReservedOrPaid = withdrawalRepository.sumWithdrawnByUserAndStatuses(
                user,
                List.of(WithdrawalStatus.PENDING, WithdrawalStatus.PROCESSING, WithdrawalStatus.COMPLETED)
        );

        BigDecimal available = balance.getAvailable().subtract(alreadyReservedOrPaid);
        return available.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : available;
    }

    @Transactional
    public Withdrawal requestWithdrawal(User user,
                                        BigDecimal amount,
                                        PaymentMethod method,
                                        String recipientPhone,
                                        String bankName,
                                        String bankAccountNumber,
                                        String bankAccountName) {
        if (user == null) {
            throw new RuntimeException("Utilisateur non authentifié.");
        }

        if (amount == null || amount.compareTo(minWithdrawalAmount) < 0) {
            throw new RuntimeException("Le montant minimum de retrait est de " + minWithdrawalAmount + " $.");
        }

        if (method == null) {
            throw new RuntimeException("Veuillez choisir une méthode de retrait.");
        }

        if (method != PaymentMethod.MONCASH && method != PaymentMethod.BANK_TRANSFER) {
            throw new RuntimeException("Méthode de retrait non supportée. Utilisez MonCash ou virement bancaire.");
        }

        BigDecimal available = getAvailableBalance(user);
        if (amount.compareTo(available) > 0) {
            throw new RuntimeException("Solde insuffisant. Disponible : " + available + " $.");
        }

        if (withdrawalRepository.existsByUserAndStatus(user, WithdrawalStatus.PENDING)) {
            throw new RuntimeException("Vous avez déjà une demande de retrait en attente. Annulez-la ou attendez son traitement.");
        }

        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setUser(user);
        withdrawal.setAmount(amount);
        withdrawal.setMethod(method);
        withdrawal.setStatus(WithdrawalStatus.PENDING);

        if (method == PaymentMethod.MONCASH) {
            String phone = normalize(recipientPhone);
            if (phone == null) {
                throw new RuntimeException("Le numéro de téléphone MonCash est requis.");
            }
            withdrawal.setRecipientPhone(phone);
        }

        if (method == PaymentMethod.BANK_TRANSFER) {
            String cleanBankName = normalize(bankName);
            String cleanAccountNumber = normalize(bankAccountNumber);
            String cleanAccountName = normalize(bankAccountName);

            if (cleanBankName == null || cleanAccountNumber == null || cleanAccountName == null) {
                throw new RuntimeException("Les informations bancaires sont incomplètes.");
            }

            withdrawal.setBankName(cleanBankName);
            withdrawal.setBankAccountNumber(cleanAccountNumber);
            withdrawal.setBankAccountName(cleanAccountName);
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
    public void cancelWithdrawal(User user, Long withdrawalId) {
        Withdrawal withdrawal = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new RuntimeException("Demande de retrait introuvable."));

        if (!withdrawal.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à annuler cette demande.");
        }

        if (withdrawal.getStatus() != WithdrawalStatus.PENDING) {
            throw new RuntimeException("Seules les demandes en attente peuvent être annulées.");
        }

        withdrawalRepository.delete(withdrawal);
    }

    @Transactional
    public void approveWithdrawal(Long withdrawalId) {
        Withdrawal withdrawal = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée."));

        if (withdrawal.getStatus() != WithdrawalStatus.PENDING) {
            throw new RuntimeException("Cette demande a déjà été traitée.");
        }

        withdrawal.setStatus(WithdrawalStatus.COMPLETED);
        withdrawal.setProcessedAt(LocalDateTime.now());
        withdrawalRepository.save(withdrawal);
    }

    @Transactional
    public void rejectWithdrawal(Long withdrawalId, String reason) {
        Withdrawal withdrawal = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée."));

        if (withdrawal.getStatus() != WithdrawalStatus.PENDING) {
            throw new RuntimeException("Cette demande a déjà été traitée.");
        }

        withdrawal.setStatus(WithdrawalStatus.REJECTED);
        withdrawal.setRejectionReason(normalize(reason) == null ? "Rejeté par l'administrateur." : reason.trim());
        withdrawal.setProcessedAt(LocalDateTime.now());
        withdrawalRepository.save(withdrawal);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
