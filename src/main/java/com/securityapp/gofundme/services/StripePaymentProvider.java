package com.securityapp.gofundme.services;

import com.securityapp.gofundme.dto.PaymentIntent;
import com.securityapp.gofundme.model.Campaign;
import com.securityapp.gofundme.model.Payment;
import com.securityapp.gofundme.model.User;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StripePaymentProvider {
    
    @Autowired
    private StripeClient stripeClient;
    
    public PaymentIntent createIntent(Payment payment, Campaign campaign, User donor) throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(payment.getAmount().multiply(new java.math.BigDecimal("100")).longValue())
                .setCurrency("eur")
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .build()
                )
                .putMetadata("campaign_id", campaign.getId().toString())
                .putMetadata("donor_email", donor.getEmail())
                .putMetadata("transaction_id", payment.getTransactionId())
                .putMetadata("platform_fee", payment.getPlatformFee().toString())
                .build();
        
        com.stripe.model.PaymentIntent intent = stripeClient.paymentIntents().create(params);
        
        return new PaymentIntent(
            intent.getClientSecret(),
            payment.getTransactionId(),
            intent.toJson()
        );
    }
    
    public void handleWebhook(String payload, String sigHeader, String secret) throws Exception {
        com.stripe.model.Event event = com.stripe.net.Webhook.constructEvent(payload, sigHeader, secret);
        
        if ("payment_intent.succeeded".equals(event.getType())) {
            com.stripe.model.PaymentIntent intent = (com.stripe.model.PaymentIntent) event.getDataObjectDeserializer()
                .getObject().orElseThrow();
            String transactionId = intent.getMetadata().get("transaction_id");
        }
    }
}