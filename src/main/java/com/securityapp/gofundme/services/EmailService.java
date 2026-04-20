///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.securityapp.gofundme.services;
//
//import jakarta.mail.internet.MimeMessage;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//
//@Service
//public class EmailService {
//
//    @Autowired
//    private JavaMailSender mailSender;
//
//    @Value("${spring.mail.username}")
//    private String fromEmail;
//
//    public void sendVerificationEmail(String to, String code) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//            helper.setFrom(fromEmail, "UnityFund");
//            helper.setTo(to);
//            helper.setSubject("Votre code de vérification UnityFund");
//
//            String html = """
//                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
//                    <h2 style="color: #10b77f;">Bienvenue sur UnityFund !</h2>
//                    <p>Pour activer votre compte, veuillez utiliser le code de vérification suivant :</p>
//                    <div style="background: #f6f8f7; padding: 20px; text-align: center; font-size: 32px; 
//                                font-weight: bold; letter-spacing: 8px; color: #10b77f; border-radius: 8px;">
//                        %s
//                    </div>
//                    <p style="color: #666; margin-top: 20px;">Ce code est valable pendant 15 minutes.</p>
//                    <p style="color: #999; font-size: 12px;">Si vous n'avez pas créé de compte, ignorez cet email.</p>
//                </div>
//                """.formatted(code);
//
//            helper.setText(html, true);
//            mailSender.send(message);
//
//        } catch (Exception e) {
//            throw new RuntimeException("Erreur envoi email: " + e.getMessage());
//        }
//    }
//}
package com.securityapp.gofundme.services;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationEmail(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "UnityFund");
            helper.setTo(to);
            helper.setSubject("Votre code de vérification UnityFund");

            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #10b77f;">Bienvenue sur UnityFund !</h2>
                    <p>Pour activer votre compte, veuillez utiliser le code de vérification suivant :</p>
                    <div style="background: #f6f8f7; padding: 20px; text-align: center; font-size: 32px; 
                                font-weight: bold; letter-spacing: 8px; color: #10b77f; border-radius: 8px;">
                        %s
                    </div>
                    <p style="color: #666; margin-top: 20px;">Ce code est valable pendant 15 minutes.</p>
                    <p style="color: #999; font-size: 12px;">Si vous n'avez pas créé de compte, ignorez cet email.</p>
                </div>
                """.formatted(code);

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Erreur envoi email: " + e.getMessage());
        }
    }

    // ========== NOUVELLE MÉTHODE ==========
    public void sendPasswordResetEmail(String to, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "UnityFund");
            helper.setTo(to);
            helper.setSubject("Réinitialisation de votre mot de passe UnityFund");

            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="text-align: center; margin-bottom: 30px;">
                        <h2 style="color: #10b77f; font-size: 28px; margin: 0;">UnityFund</h2>
                    </div>
                    
                    <h3 style="color: #333; font-size: 20px;">Réinitialisation de mot de passe</h3>
                    
                    <p style="color: #666; line-height: 1.6;">
                        Vous avez demandé la réinitialisation de votre mot de passe. 
                        Cliquez sur le bouton ci-dessous pour définir un nouveau mot de passe :
                    </p>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" 
                           style="background: #10b77f; color: white; padding: 14px 32px; text-decoration: none;
                                  border-radius: 8px; font-weight: bold; display: inline-block; font-size: 16px;">
                            Réinitialiser mon mot de passe
                        </a>
                    </div>
                    
                    <p style="color: #999; font-size: 13px; line-height: 1.5;">
                        Ce lien est valable pendant 24 heures. Si vous n'avez pas demandé cette réinitialisation, 
                        vous pouvez ignorer cet email en toute sécurité.
                    </p>
                    
                    <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                    
                    <p style="color: #bbb; font-size: 12px; text-align: center;">
                        © 2026 UnityFund. Tous droits réservés.
                    </p>
                </div>
                """.formatted(resetLink);

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Erreur envoi email reset: " + e.getMessage());
        }
    }
}