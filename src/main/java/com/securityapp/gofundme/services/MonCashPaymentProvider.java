package com.securityapp.gofundme.services;

import com.securityapp.gofundme.dto.PaymentIntent;
import com.securityapp.gofundme.model.Campaign;
import com.securityapp.gofundme.model.Payment;
import com.securityapp.gofundme.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class MonCashPaymentProvider {
    
    @Value("${moncash.client.id:}")
    private String moncashClientId;
    
    @Value("${moncash.client.secret:}")
    private String moncashSecret;
    
    // URLs exactes de la documentation officielle
    private static final String SANDBOX_BASE = "https://sandbox.moncashbutton.digicelgroup.com/Moncash-business";
    private static final String TOKEN_URL = SANDBOX_BASE + "/oauth/token";
    private static final String CREATE_URL = SANDBOX_BASE + "/Api/v1/CreatePayment";
    
    private String accessToken;
    private long tokenExpiry;
    
    private String getAccessToken() {
        // Cache le token 55 minutes
        if (accessToken != null && System.currentTimeMillis() < tokenExpiry) {
            return accessToken;
        }
        
        String credentials = moncashClientId + ":" + moncashSecret;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + encoded);
        
        HttpEntity<String> request = new HttpEntity<>("grant_type=client_credentials", headers);
        RestTemplate rest = new RestTemplate();
        
        try {
            ResponseEntity<Map> response = rest.postForEntity(TOKEN_URL, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                accessToken = (String) response.getBody().get("access_token");
                tokenExpiry = System.currentTimeMillis() + 55 * 60 * 1000; // 55 min
                return accessToken;
            }
        } catch (Exception e) {
            System.err.println("MonCash auth error: " + e.getMessage());
            throw new RuntimeException("Erreur authentification MonCash: " + e.getMessage());
        }
        throw new RuntimeException("Impossible d'obtenir le token");
    }
    
    public PaymentIntent createIntent(Payment payment, Campaign campaign, User donor) {
        String token = getAccessToken();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        
        Map<String, Object> body = new HashMap<>();
        body.put("amount", payment.getAmount().toString());
        body.put("orderId", payment.getTransactionId());
        body.put("reference", campaign.getTitle());
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        RestTemplate rest = new RestTemplate();
        
        try {
            ResponseEntity<Map> response = rest.postForEntity(CREATE_URL, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> resp = response.getBody();
                
                // Extraction du payment_token.token
                String paymentToken = "";
                if (resp.containsKey("payment_token")) {
                    Object pt = resp.get("payment_token");
                    if (pt instanceof Map) {
                        paymentToken = (String) ((Map) pt).get("token");
                    }
                }
                
                // URL de redirection pour le frontend
                String redirectUrl = "https://sandbox.moncashbutton.digicelgroup.com/Moncash-business/Checkout/Token?PaymentToken=" + paymentToken;
                
                return new PaymentIntent(paymentToken, payment.getTransactionId(), 
                    "{\"redirect\": \"" + redirectUrl + "\"}");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur création paiement: " + e.getMessage());
        }
        throw new RuntimeException("Échec création paiement");
    }
}