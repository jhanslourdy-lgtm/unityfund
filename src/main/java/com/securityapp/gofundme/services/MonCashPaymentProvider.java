package com.securityapp.gofundme.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securityapp.gofundme.dto.PaymentIntent;
import com.securityapp.gofundme.model.Campaign;
import com.securityapp.gofundme.model.Payment;
import com.securityapp.gofundme.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class MonCashPaymentProvider {
    
    @Value("${moncash.client.id:}")
    private String moncashClientId;
    
    @Value("${moncash.client.secret:}")
    private String moncashSecret;
    
    @Value("${BASE_URL:http://localhost:8080}")
    private String baseUrl;
    
    // URLs MonCash - Environnement Sandbox
    private static final String AUTH_URL = "https://sandbox.moncashbutton.digicelgroup.com/Api/oauth/token";
    private static final String CREATE_PAYMENT_URL = "https://sandbox.moncashbutton.digicelgroup.com/Api/v1/CreatePayment";
    
    // Si vous êtes en production, utilisez :
    // private static final String AUTH_URL = "https://moncashbutton.digicelgroup.com/Api/oauth/token";
    // private static final String CREATE_PAYMENT_URL = "https://moncashbutton.digicelgroup.com/Api/v1/CreatePayment";
    
    private String cachedToken;
    private long tokenExpiry;
    
    /**
     * Obtient un token d'accès OAuth2 depuis MonCash
     */
    private String getAccessToken() {
        // Vérifier si le token en cache est encore valide
        if (cachedToken != null && System.currentTimeMillis() < tokenExpiry) {
            System.out.println("MonCash: Utilisation du token en cache");
            return cachedToken;
        }
        
        System.out.println("MonCash: Demande nouveau token...");
        System.out.println("MonCash Client ID: " + (moncashClientId != null ? moncashClientId.substring(0, Math.min(8, moncashClientId.length())) + "..." : "null"));
        
        // Vérification des credentials
        if (moncashClientId == null || moncashClientId.isEmpty() || 
            moncashSecret == null || moncashSecret.isEmpty()) {
            throw new RuntimeException("MonCash credentials non configurés. Vérifiez MONCASH_CLIENT_ID et MONCASH_SECRET.");
        }
        
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            // Headers pour l'authentification Basic
            HttpHeaders headers = new HttpHeaders();
            String auth = moncashClientId + ":" + moncashSecret;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            headers.set("Authorization", "Basic " + encodedAuth);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Accept", "application/json");
            
            // Corps de la requête pour OAuth2 client_credentials
            String requestBody = "grant_type=client_credentials&scope=read,write";
            
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(AUTH_URL, request, Map.class);
            
            System.out.println("MonCash Auth Response Status: " + response.getStatusCode());
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                cachedToken = (String) body.get("access_token");
                
                // Récupérer la durée de validité (par défaut 3600 secondes)
                Integer expiresIn = (Integer) body.get("expires_in");
                if (expiresIn == null) expiresIn = 3600;
                
                // Expiration avec marge de sécurité (5 minutes avant expiration réelle)
                tokenExpiry = System.currentTimeMillis() + (expiresIn * 1000) - 300000;
                
                System.out.println("MonCash: Token obtenu avec succès, expire dans " + expiresIn + "s");
                return cachedToken;
            } else {
                System.err.println("MonCash Auth Response Body: " + response.getBody());
                throw new RuntimeException("Échec authentification MonCash: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            System.err.println("MonCash Auth Error: " + e.getMessage());
            e.printStackTrace();
            
            if (e.getMessage() != null && e.getMessage().contains("403")) {
                throw new RuntimeException("MonCash 403 Forbidden - Vérifiez que MONCASH_CLIENT_ID et MONCASH_SECRET sont corrects. " +
                    "Assurez-vous que votre compte MonCash Business est activé pour l'API.");
            }
            throw new RuntimeException("Erreur authentification MonCash: " + e.getMessage());
        }
    }
    
    /**
     * Crée une intention de paiement MonCash
     */
    public PaymentIntent createIntent(Payment payment, Campaign campaign, User donor) {
        try {
            String token = getAccessToken();
            
            // Générer un orderId unique si non défini
            String orderId = payment.getTransactionId();
            if (orderId == null || orderId.isEmpty()) {
                orderId = UUID.randomUUID().toString();
            }
            
            // Montant en format attendu par MonCash (nombre avec 2 décimales)
            double amount = payment.getAmount().doubleValue();
            
            // Construire la requête selon la documentation MonCash
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("amount", amount);
            requestBody.put("orderId", orderId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            RestTemplate restTemplate = new RestTemplate();
            
            System.out.println("MonCash CreatePayment Request: " + new ObjectMapper().writeValueAsString(requestBody));
            
            ResponseEntity<Map> response = restTemplate.postForEntity(CREATE_PAYMENT_URL, request, Map.class);
            
            System.out.println("MonCash CreatePayment Response Status: " + response.getStatusCode());
            System.out.println("MonCash CreatePayment Response Body: " + response.getBody());
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> resp = response.getBody();
                
                // Extraire le payment_token
                String paymentToken = null;
                
                // Format 1: { "payment_token": { "token": "xxx", "expires": ... } }
                if (resp.containsKey("payment_token")) {
                    Object pt = resp.get("payment_token");
                    if (pt instanceof Map) {
                        paymentToken = (String) ((Map<?, ?>) pt).get("token");
                    } else if (pt instanceof String) {
                        paymentToken = (String) pt;
                    }
                }
                
                // Format 2: { "token": "xxx" }
                if (paymentToken == null && resp.containsKey("token")) {
                    paymentToken = (String) resp.get("token");
                }
                
                // Format 3: { "redirect_url": "https://..." }
                if (resp.containsKey("redirect_url")) {
                    paymentToken = (String) resp.get("redirect_url");
                }
                
                if (paymentToken == null) {
                    System.err.println("MonCash Response complète: " + resp);
                    throw new RuntimeException("Token de paiement non trouvé dans la réponse MonCash");
                }
                
                System.out.println("MonCash Payment Token obtenu: " + paymentToken);
                
                // Sauvegarder la réponse complète pour debug
                String providerJson = new ObjectMapper().writeValueAsString(resp);
                
                return new PaymentIntent(paymentToken, orderId, providerJson);
                
            } else {
                throw new RuntimeException("Échec création paiement MonCash: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            System.err.println("MonCash CreatePayment Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur création paiement MonCash: " + e.getMessage());
        }
    }
}