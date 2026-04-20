//package com.securityapp.gofundme.services;
//
//import com.securityapp.gofundme.model.Role;
//import com.securityapp.gofundme.model.User;
//import com.securityapp.gofundme.repositories.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.Random;
//
//@Service
//public class UserService {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @Autowired
//    private EmailService emailService;
//
//    public User registerNewUser(User user) {
//        if (userRepository.existsByEmail(user.getEmail())) {
//            throw new RuntimeException("Cet email est déjà utilisé");
//        }
//
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
//        user.setRole(Role.ROLE_USER);
//        user.setEmailVerified(false);
//        
//        // Génération code à 6 chiffres
//        String code = String.format("%06d", new Random().nextInt(999999));
//        user.setVerificationCode(code);
//        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(15));
//
//        User saved = userRepository.save(user);
//        
//        // Envoi du mail
//        emailService.sendVerificationEmail(saved.getEmail(), code);
//        
//        return saved;
//    }
//
//    public void verifyEmail(String email, String code) {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
//
//        if (user.isEmailVerified()) {
//            throw new RuntimeException("Compte déjà vérifié");
//        }
//
//        if (user.getVerificationCodeExpiry() == null || 
//            user.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
//            throw new RuntimeException("Code expiré. Veuillez vous réinscrire.");
//        }
//
//        if (!user.getVerificationCode().equals(code)) {
//            throw new RuntimeException("Code incorrect");
//        }
//
//        user.setEmailVerified(true);
//        user.setVerificationCode(null);
//        user.setVerificationCodeExpiry(null);
//        userRepository.save(user);
//    }
//
//    public User findByEmail(String email) {
//        return userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
//    }
//    
//}
package com.securityapp.gofundme.services;

import com.securityapp.gofundme.model.Role;
import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    public User registerNewUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.ROLE_USER);
        user.setEmailVerified(false);
        
        String code = String.format("%06d", new Random().nextInt(999999));
        user.setVerificationCode(code);
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(15));

        User saved = userRepository.save(user);
        
        emailService.sendVerificationEmail(saved.getEmail(), code);
        
        return saved;
    }

    public void verifyEmail(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Compte déjà vérifié");
        }

        if (user.getVerificationCodeExpiry() == null || 
            user.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Code expiré. Veuillez vous réinscrire.");
        }

        if (!user.getVerificationCode().equals(code)) {
            throw new RuntimeException("Code incorrect");
        }

        user.setEmailVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        userRepository.save(user);
    }

    // ========== NOUVELLES MÉTHODES POUR RESET PASSWORD ==========

    /**
     * Demande de réinitialisation de mot de passe
     */
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElse(null);
        
        // Ne pas révéler si l'email existe ou non (sécurité)
        if (user == null) {
            return;
        }

        // Générer un token sécurisé
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(24));
        
        userRepository.save(user);
        
        // Envoyer l'email avec le lien
        String resetLink = "http://localhost:8080/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
    }

    /**
     * Vérifier la validité d'un token de reset
     */
    public User validateResetToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Token invalide");
        }

        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide ou expiré"));

        if (user.getResetTokenExpiry() == null || 
            user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Ce lien a expiré. Veuillez faire une nouvelle demande.");
        }

        return user;
    }

    /**
     * Réinitialiser le mot de passe
     */
    public void resetPassword(String token, String newPassword) {
        User user = validateResetToken(token);
        
        // Valider la force du mot de passe
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("Le mot de passe doit contenir au moins 6 caractères");
        }

        // Hasher et sauvegarder
        user.setPassword(passwordEncoder.encode(newPassword));
        
        // Invalider le token immédiatement (one-time use)
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        
        userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }
    public User findById(Long id) {
    return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
}

public void updateUser(User user) {
    userRepository.save(user);
}
}