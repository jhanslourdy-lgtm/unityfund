///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.securityapp.gofundme.config;
//
//import java.math.BigDecimal;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// *
// * @author Handy
// */
//// PaymentConfig.java
//@Configuration
//public class PaymentConfig {
//    
//    @Value("${stripe.api.key}")
//    private String stripeApiKey;
//    
//    @Value("${stripe.webhook.secret}")
//    private String stripeWebhookSecret;
//    
//    @Value("${moncash.client.id}")
//    private String moncashClientId;
//    
//    @Value("${moncash.client.secret}")
//    private String moncashSecret;
//    
//    @Value("${platform.fee.percentage:0.05}") // 5% frais plateforme
//    private BigDecimal platformFeePercentage;
//    
//    @Bean
//    public StripeClient stripeClient() {
//        return new StripeClient(stripeApiKey);
//    }
//}
package com.securityapp.gofundme.config;

import com.stripe.StripeClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentConfig {
    
    @Value("${stripe.api.key:}")
    private String stripeApiKey;
    
    @Bean
    public StripeClient stripeClient() {
        return new StripeClient(stripeApiKey);
    }
}