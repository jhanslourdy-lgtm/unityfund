package com.securityapp.gofundme.services;

import com.digicelgroup.moncash.APIContext;
import com.digicelgroup.moncash.http.Constants;
import com.digicelgroup.moncash.payments.Payment;
import com.digicelgroup.moncash.payments.PaymentCapture;
import com.digicelgroup.moncash.payments.PaymentCreator;
import com.digicelgroup.moncash.payments.TransactionId;
import com.securityapp.gofundme.dto.PaymentIntent;
import com.securityapp.gofundme.model.Campaign;
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

    public PaymentIntent createIntent(com.securityapp.gofundme.model.Payment payment, 
                                      Campaign campaign, User donor) {
        if (moncashClientId.isBlank() || moncashSecret.isBlank()) {
            throw new RuntimeException("MonCash credentials non configurés");
        }

        // 1. Contexte API en mode sandbox
        APIContext apiContext = new APIContext(moncashClientId, moncashSecret, Constants.SANDBOX);

        // 2. Création du paiement
        Payment moncashPayment = new Payment();
        moncashPayment.setOrderId(payment.getTransactionId());
        moncashPayment.setAmount(payment.getAmount().doubleValue());

        PaymentCreator creator = new PaymentCreator();
        
        try {
            creator = creator.execute(apiContext, PaymentCreator.class, moncashPayment);
        } catch (Exception e) {
            throw new RuntimeException("Erreur création paiement MonCash: " + e.getMessage());
        }

        // 3. Vérification réponse
        if (creator.getStatus() != null && 
            creator.getStatus().compareTo(HttpStatus.SC_ACCEPTED + "") == 0) {
            
            // L'URL officielle de checkout !
            String redirectUrl = creator.redirectUri();
            
            return new PaymentIntent(redirectUrl, payment.getTransactionId(), creator.toString());
            
        } else if (creator.getStatus() == null) {
            throw new RuntimeException("MonCash: " + creator.getError() + " - " + creator.getError_description());
        } else {
            throw new RuntimeException("MonCash: " + creator.getStatus() + " | " + creator.getMessage());
        }
    }

    public boolean verifyPayment(String transactionId) {
        try {
            APIContext apiContext = new APIContext(moncashClientId, moncashSecret, Constants.SANDBOX);
            
            PaymentCapture capture = new PaymentCapture();
            TransactionId tid = new TransactionId();
            tid.setTransactionId(transactionId);
            
            PaymentCapture result = capture.execute(apiContext, PaymentCapture.class, tid);
            
            return result.getStatus() != null && 
                   result.getStatus().compareTo(HttpStatus.SC_OK + "") == 0;
                   
        } catch (Exception e) {
            System.err.println("Erreur vérification MonCash: " + e.getMessage());
            return false;
        }
    }
}