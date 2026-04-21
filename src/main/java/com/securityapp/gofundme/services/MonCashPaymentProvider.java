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
    
    @Value("${moncash.api.url:https://sandbox.moncashbutton.digicelgroup.com}")
    private String apiUrl;
    
    @Value("${moncash.client.id:}")
    private String moncashClientId;
    
    @Value("${moncash.client.secret:}")
    private String moncashSecret;
    
    /**
     * Récupère un token d'accès OAuth2 MonCash - VERSION CORRIGÉE
     */
    private String getAccessToken() {
        // MonCash utilise l'endpoint /oauth/token directement sur le domaine principal
        String tokenUrl = "https://sandbox.moncashbutton.digicelgroup.com/Moncash-business/oauth/token";
        
        // Créer le header Authorization Basic (client_id:client_secret en base64)
        String credentials = moncashClientId + ":" + moncashSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + encodedCredentials);
        
        // Body selon la spec OAuth2 client_credentials
        String body = "grant_type=client_credentials";
        
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String accessToken = (String) responseBody.get("access_token");
                
                if (accessToken == null || accessToken.isEmpty()) {
                    throw new RuntimeException("Token d'accès vide dans la réponse MonCash");
                }
                
                return accessToken;
            }
        } catch (Exception e) {
            System.err.println("Erreur détaillée authentification MonCash: " + e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("Bad authority")) {
                System.err.println("→ Vérifiez que MONCASH_CLIENT_ID et MONCASH_SECRET sont corrects");
                System.err.println("→ Client ID utilisé: " + moncashClientId.substring(0, Math.min(10, moncashClientId.length())) + "...");
            }
            throw new RuntimeException("Erreur authentification MonCash: " + e.getMessage());
        }
        throw new RuntimeException("Impossible d'obtenir le token MonCash");
    }
    
    public PaymentIntent createIntent(Payment payment, Campaign campaign, User donor) {
        String orderId = "UNITY-" + System.currentTimeMillis();
        
        // 1. Récupérer le token OAuth2
        String accessToken = getAccessToken();
        
        // 2. Créer le paiement avec le Bearer token
        String createPaymentUrl = "https://sandbox.moncashbutton.digicelgroup.com/Moncash-business/Api/v1/CreatePayment";
        
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
            ResponseEntity<Map> response = restTemplate.postForEntity(createPaymentUrl, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                
                // Extraction du token de paiement
                String token = extractPaymentToken(responseBody);
                
                return new PaymentIntent(token, payment.getTransactionId(), responseBody.toString());
            }
        } catch (Exception e) {
            System.err.println("Erreur création paiement MonCash: " + e.getMessage());
            throw new RuntimeException("Erreur création paiement MonCash: " + e.getMessage());
        }
        
        throw new RuntimeException("Impossible de créer le paiement MonCash");
    }
    
    /**
     * Extrait le token de paiement de la réponse MonCash
     */
    private String extractPaymentToken(Map<String, Object> responseBody) {
        try {
            if (responseBody.containsKey("payment_token")) {
                Object paymentToken = responseBody.get("payment_token");
                
                if (paymentToken instanceof Map) {
                    Map<String, Object> tokenMap = (Map<String, Object>) paymentToken;
                    return (String) tokenMap.get("token");
                } else if (paymentToken instanceof String) {
                    return (String) paymentToken;
                }
            }
            
            // Fallback: chercher dans d'autres champs possibles
            if (responseBody.containsKey("token")) {
                return (String) responseBody.get("token");
            }
            if (responseBody.containsKey("paymentToken")) {
                return (String) responseBody.get("paymentToken");
            }
            
        } catch (Exception e) {
            System.err.println("Structure réponse MonCash: " + responseBody);
        }
        
        throw new RuntimeException("Impossible d'extraire le token de paiement de: " + responseBody);
    }
    
    /**
     * Vérifie le statut d'un paiement MonCash
     */
    public boolean verifyPayment(String orderId) {
        try {
            String accessToken = getAccessToken();
            
            String verifyUrl = "https://sandbox.moncashbutton.digicelgroup.com/Moncash-business/Api/v1/RetrieveTransactionPayment?orderId=" + orderId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(
                verifyUrl,
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