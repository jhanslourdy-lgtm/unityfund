package com.securityapp.gofundme.services;

import com.securityapp.gofundme.dto.PaymentIntent;
import com.securityapp.gofundme.model.Campaign;
import com.securityapp.gofundme.model.Payment;
import com.securityapp.gofundme.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class MonCashPaymentProvider {
    
    @Value("${moncash.api.url:https://sandbox.moncashbutton.digicelgroup.com}")
    private String apiUrl;
    
    @Value("${moncash.client.id:}")
    private String moncashClientId;
    
    @Value("${moncash.client.secret:}")
    private String moncashSecret;
    
    public PaymentIntent createIntent(Payment payment, Campaign campaign, User donor) {
        String orderId = "UNITY-" + System.currentTimeMillis();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(moncashClientId, moncashSecret);
        
        Map<String, Object> body = new HashMap<>();
        body.put("amount", payment.getAmount().toString());
        body.put("orderId", orderId);
        body.put("reference", payment.getTransactionId());
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                apiUrl + "/api/v1/CreatePayment", 
                request, 
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String token = "";
                if (responseBody.containsKey("payment_token")) {
                    Map<String, String> tokenMap = (Map<String, String>) responseBody.get("payment_token");
                    token = tokenMap.get("token");
                }
                return new PaymentIntent(token, payment.getTransactionId(), responseBody.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur création paiement MonCash: " + e.getMessage());
        }
        
        throw new RuntimeException("Impossible de créer le paiement MonCash");
    }
}