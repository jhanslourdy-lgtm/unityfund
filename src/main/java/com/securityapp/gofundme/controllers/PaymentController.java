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
            stripeProvider.handleWebhook(payload, sigHeader, secret);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Erreur webhook");
        }
    }
    
    @GetMapping("/callback/moncash")
    public String handleMonCashCallback(
            @RequestParam("transactionId") String transactionId,
            @RequestParam(value = "status", defaultValue = "FAILED") String status) {
        try {
            if ("SUCCESS".equals(status)) {
                paymentService.confirmPayment(transactionId, "{\"status\": \"SUCCESS\"}");
                return "redirect:/payment/success";
            }
            return "redirect:/payment/failed";
        } catch (Exception e) {
            return "redirect:/payment/error";
        }
    }
}