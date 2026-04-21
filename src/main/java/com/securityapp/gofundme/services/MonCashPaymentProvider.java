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
    
    /**
     * Récupère un token d'accès OAuth2 MonCash
     */
    private String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        // MonCash utilise grant_type=client_credentials dans le body
        String body = "grant_type=client_credentials&client_id=" + moncashClientId 
                    + "&client_secret=" + moncashSecret;
        
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                apiUrl + "/oauth/token", 
                request, 
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                return (String) responseBody.get("access_token");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur authentification MonCash: " + e.getMessage());
        }
        throw new RuntimeException("Impossible d'obtenir le token MonCash");
    }
    
    public PaymentIntent createIntent(Payment payment, Campaign campaign, User donor) {
        String orderId = "UNITY-" + System.currentTimeMillis();
        
        // 1. Récupérer le token OAuth2
        String accessToken = getAccessToken();
        
        // 2. Créer le paiement avec le Bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken); // Authorization: Bearer <token>
        
        Map<String, Object> body = new HashMap<>();
        body.put("amount", payment.getAmount().toString());
        body.put("orderId", orderId);
        body.put("reference", payment.getTransactionId());
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                apiUrl + "/Api/v1/CreatePayment", 
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
    
    /**
     * Vérifie le statut d'un paiement MonCash
     */
    public boolean verifyPayment(String orderId) {
        try {
            String accessToken = getAccessToken();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl + "/Api/v1/RetrieveTransactionPayment?orderId=" + orderId,
                HttpMethod.GET,
                request,
                Map.class
            );
            
            if (response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                return body.containsKey("status") && "SUCCESS".equals(body.get("status"));
            }
        } catch (Exception e) {
            System.err.println("Erreur vérification MonCash: " + e.getMessage());
        }
        return false;
    }
}