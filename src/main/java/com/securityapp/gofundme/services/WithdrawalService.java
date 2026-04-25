package com.securityapp.gofundme.services;

import com.securityapp.gofundme.dto.CreatorBalance;
import com.securityapp.gofundme.model.*;
import com.securityapp.gofundme.repositories.CampaignRepository;
import com.securityapp.gofundme.repositories.PaymentRepository;
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

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Value("${withdrawal.min.amount:10.00}")
    private BigDecimal minWithdrawalAmount;

    public BigDecimal getAvailableBalance(User user) {
        CreatorBalance balance = financialService.calculateBalance(user);
        BigDecimal alreadyWithdrawn = withdrawalRepository.sumWithdrawnByUser(user);
        BigDecimal available = balance.getAvailable().subtract(alreadyWithdrawn);
        return available.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : available;
    }

    public BigDecimal getAvailableBalanceForCampaign(Campaign campaign) {
        BigDecimal receivedNet = paymentRepository.sumNetAmountByCampaignAndStatus(campaign, PaymentStatus.COMPLETED);
        BigDecimal alreadyWithdrawn = withdrawalRepository.sumWithdrawnByCampaign(campaign);
        BigDecimal available = receivedNet.subtract(alreadyWithdrawn);
        return available.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : available;
    }

    @Transactional
    public Withdrawal requestWithdrawal(User user,
                                        Long campaignId,
                                        BigDecimal amount,
                                        PaymentMethod method,
                                        String recipientPhone,
                                        String bankName,
                                        String bankAccountNumber,
                                        String bankAccountName) {

        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campagne introuvable."));

        if (campaign.getUser() == null || !campaign.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Vous ne pouvez retirer que les fonds de vos propres campagnes.");
        }

        if (campaign.getStatus() != CampaignStatus.ACTIVE && campaign.getStatus() != CampaignStatus.COMPLETED) {
            throw new RuntimeException("Cette campagne ne permet pas de demande de retrait.");
        }

        if (amount == null || amount.compareTo(minWithdrawalAmount) < 0) {
            throw new RuntimeException("Le montant minimum de retrait est de " + minWithdrawalAmount + " $");
        }

        BigDecimal available = getAvailableBalanceForCampaign(campaign);
        if (amount.compareTo(available) > 0) {
            throw new RuntimeException("Solde insuffisant pour cette campagne. Disponible : " + available + " $");
        }

        if (withdrawalRepository.existsByCampaignAndStatus(campaign, WithdrawalStatus.PENDING)) {
            throw new RuntimeException("Cette campagne a déjà une demande de retrait en attente.");
        }

        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setUser(user);
        withdrawal.setCampaign(campaign);
        withdrawal.setAmount(amount);
        withdrawal.setMethod(method);
        withdrawal.setStatus(WithdrawalStatus.PENDING);

        if (method == PaymentMethod.MONCASH) {
            if (recipientPhone == null || recipientPhone.isBlank()) {
                throw new RuntimeException("Le numéro de téléphone est requis pour MonCash.");
            }
            withdrawal.setRecipientPhone(recipientPhone.trim());
        } else if (method == PaymentMethod.BANK_TRANSFER) {
            if (bankName == null || bankName.isBlank()
                    || bankAccountNumber == null || bankAccountNumber.isBlank()
                    || bankAccountName == null || bankAccountName.isBlank()) {
                throw new RuntimeException("Les informations bancaires sont incomplètes.");
            }
            withdrawal.setBankName(bankName.trim());
            withdrawal.setBankAccountNumber(bankAccountNumber.trim());
            withdrawal.setBankAccountName(bankAccountName.trim());
        } else {
            throw new RuntimeException("Méthode de retrait non supportée.");
        }

        Withdrawal saved = withdrawalRepository.save(withdrawal);

        // Règle métier demandée : dès qu'un retrait est demandé, la cagnotte ne reste plus active.
        // Elle n'apparaîtra donc plus comme une campagne ouverte à la collecte.
        campaign.setStatus(CampaignStatus.COMPLETED);
        campaignRepository.save(campaign);

        return saved;
    }

    /** Compatibilité avec les anciens appels sans campaignId. */
    @Transactional
    public Withdrawal requestWithdrawal(User user, BigDecimal amount, PaymentMethod method,
                                        String recipientPhone, String bankName,
                                        String bankAccountNumber, String bankAccountName) {
        List<Campaign> campaigns = campaignRepository.findByUser(user);
        Campaign firstEligible = campaigns.stream()
                .filter(c -> getAvailableBalanceForCampaign(c).compareTo(minWithdrawalAmount) >= 0)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Aucune campagne ne possède un solde retirable."));

        return requestWithdrawal(user, firstEligible.getId(), amount, method, recipientPhone,
                bankName, bankAccountNumber, bankAccountName);
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

        // Si le retrait est rejeté, on peut rouvrir la campagne.
        if (w.getCampaign() != null && w.getCampaign().getStatus() == CampaignStatus.COMPLETED) {
            w.getCampaign().setStatus(CampaignStatus.ACTIVE);
            campaignRepository.save(w.getCampaign());
        }
    }
}
