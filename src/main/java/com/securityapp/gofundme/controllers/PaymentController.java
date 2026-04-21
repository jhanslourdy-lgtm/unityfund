package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.dto.DonationRequest;
import com.securityapp.gofundme.dto.PaymentIntent;
import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.services.PaymentService;
import com.securityapp.gofundme.services.StripePaymentProvider;
import com.securityapp.gofundme.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/api/payments")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private StripePaymentProvider stripeProvider;
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/create-intent")
    @ResponseBody
    public ResponseEntity<?> createPaymentIntent(
            @RequestBody @Valid DonationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User donor = userService.findByEmail(userDetails.getUsername());
            PaymentIntent intent = paymentService.createPaymentIntent(request, donor);
            return ResponseEntity.ok(intent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/webhook/stripe")
    @ResponseBody
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader,
            @Value("${stripe.webhook.secret:}") String secret) {
        try {
            // Vérifier que le secret est configuré
            if (secret == null || secret.isEmpty() || secret.contains("localhost")) {
                return ResponseEntity.status(500).body("Webhook secret non configuré");
            }
            
            String transactionId = stripeProvider.handleWebhook(payload, sigHeader, secret);
            
            if (transactionId != null) {
                // Confirmer le paiement en base (met à jour la campagne, etc.)
                paymentService.confirmPayment(transactionId, "{\"status\": \"SUCCESS\", \"provider\": \"stripe\"}");
            }
            
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Erreur webhook: " + e.getMessage());
        }
    }
    /**
 * Confirmation manuelle après succès Stripe (fallback si webhook lent/absent)
 */
@PostMapping("/confirm")
@ResponseBody
public ResponseEntity<?> confirmPaymentManually(@RequestBody Map<String, String> payload) {
    try {
        String transactionId = payload.get("transactionId");
        paymentService.confirmPayment(transactionId, "{\"status\": \"SUCCESS\", \"provider\": \"stripe_manual\"}");
        return ResponseEntity.ok(Map.of("success", true));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
 @GetMapping("/callback/moncash")
public String handleMonCashCallback(
        @RequestParam(value = "transactionId", required = false) String transactionId,
        @RequestParam(value = "orderId", required = false) String orderId,
        @RequestParam(value = "status", defaultValue = "FAILED") String status) {
    try {
        // MonCash peut renvoyer orderId ou transactionId selon la config
        String effectiveTransactionId = (transactionId != null) ? transactionId : orderId;
        
        if (effectiveTransactionId == null) {
            return "redirect:/payment/failed";
        }
        
        // En sandbox, on considère que le retour = succès si on arrive ici
        // (MonCash redirige vers success URL uniquement si payé)
        paymentService.confirmPayment(effectiveTransactionId, "{\"status\": \"SUCCESS\", \"provider\": \"moncash\"}");
        return "redirect:/donation/success?transactionId=" + effectiveTransactionId;
        
    } catch (Exception e) {
        return "redirect:/payment/failed";
    }
}
}