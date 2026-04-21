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
    
    /**
     * Crée un paiement MonCash via le SDK officiel
     */
    public PaymentIntent createIntent(com.securityapp.gofundme.model.Payment payment, Campaign campaign, User donor) {
        try {
            // 1. Initialiser le contexte API (Sandbox)
            APIContext apiContext = new APIContext(moncashClientId, moncashSecret, Constants.SANDBOX);
            
            // 2. Créer l'objet Payment MonCash
            Payment moncashPayment = new Payment();
            moncashPayment.setOrderId(payment.getTransactionId()); // Utilise notre transactionId comme orderId
            moncashPayment.setAmount(payment.getAmount().intValue()); // MonCash attend un entier
            
            // 3. Exécuter la création
            PaymentCreator paymentCreator = new PaymentCreator();
            PaymentCreator creator = paymentCreator.execute(apiContext, PaymentCreator.class, moncashPayment);
            
            // 4. Vérifier le statut
            if (creator.getStatus() != null && creator.getStatus().equals(HttpStatus.SC_ACCEPTED + "")) {
                // Succès - récupérer l'URL de redirection
                String redirectUri = creator.redirectUri();
                
                // Le token est dans l'URL de redirection : https://.../Token?PaymentToken=XXXX
                // On extrait le PaymentToken pour le frontend
                String paymentToken = extractPaymentToken(redirectUri);
                
                return new PaymentIntent(
                    paymentToken,           // Le token à passer au frontend
                    payment.getTransactionId(),
                    "{\"redirectUri\": \"" + redirectUri + "\"}"
                );
                
            } else if (creator.getStatus() == null) {
                throw new RuntimeException("Erreur MonCash: " + creator.getError() + " - " + creator.getError_description());
            } else {
                throw new RuntimeException("Erreur MonCash: Status=" + creator.getStatus() 
                    + ", Error=" + creator.getError() 
                    + ", Message=" + creator.getMessage()
                    + ", Path=" + creator.getPath());
            }
            
        } catch (Exception e) {
            System.err.println("MonCash Debug - Erreur complète: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur création paiement MonCash: " + e.getMessage());
        }
    }
    
    /**
     * Extrait le PaymentToken de l'URL de redirection MonCash
     * URL format: https://sandbox.moncashbutton.digicelgroup.com/Moncash-business/Checkout/Token?PaymentToken=XXXX
     */
    private String extractPaymentToken(String redirectUri) {
        if (redirectUri == null || redirectUri.isEmpty()) {
            throw new RuntimeException("URL de redirection MonCash vide");
        }
        
        try {
            java.net.URL url = new java.net.URL(redirectUri);
            String query = url.getQuery();
            
            if (query != null && query.contains("PaymentToken=")) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("PaymentToken=")) {
                        return param.substring("PaymentToken=".length());
                    }
                }
            }
            
            // Fallback: retourner l'URL complète si on n'arrive pas à extraire
            System.out.println("MonCash Debug - URL complète: " + redirectUri);
            return redirectUri;
            
        } catch (Exception e) {
            System.err.println("MonCash Debug - Impossible de parser l'URL: " + redirectUri);
            return redirectUri;
        }
    }
    
    /**
     * Vérifie un paiement par transactionId via le SDK
     */
    public boolean verifyPayment(String transactionId) {
        try {
            APIContext apiContext = new APIContext(moncashClientId, moncashSecret, Constants.SANDBOX);
            
            PaymentCapture paymentCapture = new PaymentCapture();
            TransactionId tid = new TransactionId();
            tid.setTransactionId(transactionId);
            
            PaymentCapture capture = paymentCapture.execute(apiContext, PaymentCapture.class, tid);
            
            if (capture.getStatus() != null && capture.getStatus().equals(HttpStatus.SC_OK + "")) {
                return capture.getPayment() != null && "SUCCESS".equals(capture.getPayment().getMessage());
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println("Erreur vérification MonCash: " + e.getMessage());
            return false;
        }
    }
}