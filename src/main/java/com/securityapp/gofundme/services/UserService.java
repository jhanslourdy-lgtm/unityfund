package com.securityapp.gofundme.services;

import com.securityapp.gofundme.model.PasswordResetToken;
import com.securityapp.gofundme.model.Role;
import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.model.VerificationToken;
import com.securityapp.gofundme.repositories.PasswordResetTokenRepository;
import com.securityapp.gofundme.repositories.UserRepository;
import com.securityapp.gofundme.repositories.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;

@Service
public class UserService {
    @Autowired
private PasswordResetTokenRepository passwordResetTokenRepository;

@Autowired
private UserRepository userRepository;


    @Autowired
private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;
    
     @Value("${BASE_URL:http://localhost:8080}")
    private String baseUrl;

   public User registerUser(User user) {

    User saved = userRepository.save(user);

    String token = UUID.randomUUID().toString();

    VerificationToken vt = new VerificationToken();
    vt.setToken(token);
    vt.setUser(saved);
    vt.setExpiryDate(LocalDateTime.now().plusHours(24));

    verificationTokenRepository.save(vt);

    emailService.sendVerificationEmail(saved.getEmail(), token);
    

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
    public void resetPassword(String token, String newPassword) {

    PasswordResetToken prt = passwordResetTokenRepository.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Token invalide"));

    if (prt.isUsed()) {
        throw new RuntimeException("Token déjà utilisé");
    }

    if (prt.getExpiryDate().isBefore(LocalDateTime.now())) {
        throw new RuntimeException("Token expiré");
    }

    User user = prt.getUser();

    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    prt.setUsed(true);
    passwordResetTokenRepository.save(prt);

   
}

    // ========== NOUVELLES MÉTHODES POUR RESET PASSWORD ==========

    /**
     * Demande de réinitialisation de mot de passe
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
public void requestPasswordReset(String email) {

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    String token = UUID.randomUUID().toString();

    PasswordResetToken prt = new PasswordResetToken();
    prt.setToken(token);
    prt.setUser(user);
    prt.setExpiryDate(LocalDateTime.now().plusHours(1));

    passwordResetTokenRepository.save(prt);

    emailService.sendResetPasswordEmail(email, token);

   
}
    /**
     * Réinitialiser le mot de passe
     */

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