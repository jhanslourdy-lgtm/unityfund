package com.securityapp.gofundme.services;

import com.digicelgroup.moncash.APIContext;
import com.digicelgroup.moncash.payments.Payment;
import com.digicelgroup.moncash.payments.PaymentCapture;
import com.digicelgroup.moncash.payments.PaymentCreator;
import com.digicelgroup.moncash.payments.TransactionId;
import com.securityapp.gofundme.dto.PaymentIntent;
import com.securityapp.gofundme.model.Campaign;
import com.securityapp.gofundme.model.Payment;
import com.securityapp.gofundme.model.User;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MonCashPaymentProvider {
    
    @Value("${moncash.client.id:}")
    private String moncashClientId;
    
    @Value("${moncash.client.secret:}")
    private String moncashSecret;
    
    /**
     * Crée un paiement MonCash via le SDK officiel
     * Retourne le PaymentIntent avec l'URL de redirection dans clientSecret
     */
    public PaymentIntent createIntent(Payment payment, Campaign campaign, User donor) {
        // Vérification des credentials
        if (moncashClientId == null || moncashClientId.isBlank() || 
            moncashSecret == null || moncashSecret.isBlank()) {
            throw new RuntimeException("MonCash credentials non configurés");
        }

        // 1. Initialisation du contexte API
        APIContext apiContext = new APIContext(moncashClientId, moncashSecret);
        
        // 2. Création du paiement
        PaymentCreator paymentCreator = new PaymentCreator();
        Payment moncashPayment = new Payment();
        moncashPayment.setOrderId(payment.getTransactionId());
        moncashPayment.setAmount(payment.getAmount().doubleValue());
        
        // 3. Exécution
        PaymentCreator creator = paymentCreator.execute(apiContext, PaymentCreator.class, moncashPayment);
        
        // 4. Vérification et récupération de l'URL
        if (creator.getStatus() != null && 
            creator.getStatus().compareTo(HttpStatus.SC_ACCEPTED + "") == 0) {
            
            // L'URL de checkout est ici !
            String redirectUrl = creator.redirectUri();
            
            return new PaymentIntent(redirectUrl, payment.getTransactionId(), creator.toString());
            
        } else if (creator.getStatus() == null) {
            throw new RuntimeException("MonCash error: " + creator.getError() + " - " + creator.getError_description());
        } else {
            throw new RuntimeException("MonCash error: " + creator.getStatus() + " | " + creator.getMessage() + " | " + creator.getPath());
        }
    }
    
    /**
     * Vérifie une transaction par son ID (utilisé dans le callback)
     */
    public boolean verifyPayment(String transactionIdStr) {
        try {
            APIContext apiContext = new APIContext(moncashClientId, moncashSecret);
            
            PaymentCapture paymentCapture = new PaymentCapture();
            TransactionId tid = new TransactionId();
            tid.setTransactionId(transactionIdStr);
            
            PaymentCapture capture = paymentCapture.execute(apiContext, PaymentCapture.class, tid);
            
            if (capture.getStatus() != null && 
                capture.getStatus().compareTo(HttpStatus.SC_OK + "") == 0) {
                
                System.out.println("MonCash transaction validée: " + capture.getPayment().getTransaction_id());
                System.out.println("Payer: " + capture.getPayment().getPayer());
                System.out.println("Amount: " + capture.getPayment().getCost());
                return true;
            }
            
            System.err.println("MonCash verification failed: " + capture.getStatus());
            return false;
            
        } catch (Exception e) {
            System.err.println("Erreur vérification MonCash: " + e.getMessage());
            return false;
        }
    }
}