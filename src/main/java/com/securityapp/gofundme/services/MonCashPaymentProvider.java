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
    
    // URLs officielles MonCash Sandbox
    private static final String SANDBOX_BASE_URL = "https://sandbox.moncashbutton.digicelgroup.com";
    private static final String TOKEN_ENDPOINT = "/oauth/token";
    private static final String CREATE_PAYMENT_ENDPOINT = "/Api/v1/CreatePayment";
    private static final String RETRIEVE_PAYMENT_ENDPOINT = "/Api/v1/RetrieveTransactionPayment";
    
    /**
     * Récupère un token d'accès OAuth2 MonCash
     */
    private String getAccessToken() {
        String tokenUrl = SANDBOX_BASE_URL + TOKEN_ENDPOINT;
        
        // Header Authorization: Basic base64(client_id:client_secret)
        String credentials = moncashClientId + ":" + moncashSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + encodedCredentials);
        
        // Body: grant_type=client_credentials
        String body = "grant_type=client_credentials";
        
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String accessToken = (String) response.getBody().get("access_token");
                
                if (accessToken == null || accessToken.isEmpty()) {
                    throw new RuntimeException("Token d'accès vide dans la réponse");
                }
                
                return accessToken;
            }
        } catch (Exception e) {
            System.err.println("Erreur auth MonCash: " + e.getMessage());
            throw new RuntimeException("Erreur authentification MonCash: " + e.getMessage());
        }
        
        throw new RuntimeException("Impossible d'obtenir le token MonCash");
    }
    
    public PaymentIntent createIntent(Payment payment, Campaign campaign, User donor) {
        String orderId = "UNITY-" + System.currentTimeMillis();
        String accessToken = getAccessToken();
        
        String createUrl = SANDBOX_BASE_URL + CREATE_PAYMENT_ENDPOINT;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        
        Map<String, Object> body = new HashMap<>();
        body.put("amount", payment.getAmount().toString());
        body.put("orderId", orderId);
        body.put("reference", payment.getTransactionId());
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(createUrl, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String token = extractPaymentToken(responseBody);
                return new PaymentIntent(token, payment.getTransactionId(), responseBody.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur création paiement MonCash: " + e.getMessage());
        }
        
        throw new RuntimeException("Impossible de créer le paiement MonCash");
    }
    
    private String extractPaymentToken(Map<String, Object> responseBody) {
        try {
            if (responseBody.containsKey("payment_token")) {
                Object paymentToken = responseBody.get("payment_token");
                if (paymentToken instanceof Map) {
                    Map<String, Object> tokenMap = (Map<String, Object>) paymentToken;
                    return (String) tokenMap.get("token");
                }
            }
        } catch (Exception e) {
            System.err.println("Structure réponse: " + responseBody);
        }
        throw new RuntimeException("Impossible d'extraire le token");
    }
    
    public boolean verifyPayment(String orderId) {
        try {
            String accessToken = getAccessToken();
            String verifyUrl = SANDBOX_BASE_URL + RETRIEVE_PAYMENT_ENDPOINT + "?orderId=" + orderId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(
                verifyUrl, HttpMethod.GET, request, Map.class
            );
            
            if (response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                return body.containsKey("status") && "SUCCESS".equals(body.get("status"));
            }
        } catch (Exception e) {
            System.err.println("Erreur vérification: " + e.getMessage());
        }
        return false;
    }
}