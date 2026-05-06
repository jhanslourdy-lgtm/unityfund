package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.model.VerificationToken;
import com.securityapp.gofundme.repositories.UserRepository;
import com.securityapp.gofundme.repositories.VerificationTokenRepository;
import com.securityapp.gofundme.services.EmailService;
import com.securityapp.gofundme.services.UserService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
@Autowired
private VerificationTokenRepository verificationTokenRepository;

@Autowired
private UserRepository userRepository;
    @Autowired
    private UserService userService;
 @Autowired
    private EmailService emailService;
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }
@PostMapping("/resend-verification")
public ResponseEntity<?> resend(@RequestParam String email) {

    User user = userRepository.findByEmail(email)
            .orElseThrow();

    String token = UUID.randomUUID().toString();

    VerificationToken vt = new VerificationToken();
    vt.setToken(token);
    vt.setUser(user);
    vt.setExpiryDate(LocalDateTime.now().plusHours(24));

    verificationTokenRepository.save(vt);

    emailService.sendVerificationEmail(email, token);

    return ResponseEntity.ok("Email renvoyé");
}
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, 
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "register";
        }
        try {
//            userService.registerNewUser(user);
            return "redirect:/verify?email=" + user.getEmail();
        } catch (RuntimeException e) {
            result.rejectValue("email", "error.user", e.getMessage());
            return "register";
        }
    }

@GetMapping("/verify")
public String verify(@RequestParam String token) {

    VerificationToken vt = verificationTokenRepository.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Token invalide"));

    if (vt.getExpiryDate().isBefore(LocalDateTime.now())) {
        return "token_expired";
    }

    User user = vt.getUser();
    user.setEnabled(true);

    userRepository.save(user);

    return "verified_success";
}
    @PostMapping("/verify")
    public String verifyEmail(@RequestParam String email, 
                              @RequestParam String code,
                              RedirectAttributes redirectAttributes) {
        try {
            userService.verifyEmail(email, code);
            redirectAttributes.addFlashAttribute("verified", true);
            return "redirect:/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/verify?email=" + email;
        }
    }
    @PostMapping("/forgot-password")
public ResponseEntity<?> forgot(@RequestParam String email) {

    userService.requestPasswordReset(email);

    return ResponseEntity.ok("Email envoyé");
}
@GetMapping("/reset-password")
public String resetPage(@RequestParam String token, Model model) {
    model.addAttribute("token", token);
    return "reset_password";
}
@PostMapping("/reset-password")
public String resetSubmit(@RequestParam String token,
                          @RequestParam String password) {

    userService.resetPassword(token, password);

    return "redirect:/login?resetSuccess";
}

    // ========== NOUVEAUX ENDPOINTS POUR MOT DE PASSE OUBLIÉ ==========

    /**
     * Affiche le formulaire "Mot de passe oublié"
     */
//    @GetMapping("/forgot-password")
//    public String showForgotPasswordForm() {
//        return "forgot-password";
//    }
//
//    /**
//     * Traite la demande de reset et envoie l'email
//     */
//    @PostMapping("/forgot-password")
//    public String processForgotPassword(@RequestParam String email,
//                                        RedirectAttributes redirectAttributes) {
//        try {
//            userService.requestPasswordReset(email);
//            // Message générique pour ne pas révéler si l'email existe
//            redirectAttributes.addFlashAttribute("success", 
//                "Si un compte existe avec cet email, un lien de réinitialisation a été envoyé.");
//            return "redirect:/forgot-password";
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", 
//                "Une erreur est survenue. Veuillez réessayer.");
//            return "redirect:/forgot-password";
//        }
//    }
//
//    /**
//     * Affiche le formulaire de nouveau mot de passe (depuis le lien email)
//     */
//    @GetMapping("/reset-password")
//    public String showResetPasswordForm(@RequestParam String token, Model model) {
//        try {
//            User user = userService.validateResetToken(token);
//            model.addAttribute("token", token);
//            model.addAttribute("email", user.getEmail());
//            return "reset-password";
//        } catch (RuntimeException e) {
//            model.addAttribute("error", e.getMessage());
//            model.addAttribute("invalidToken", true);
//            return "reset-password";
//        }
//    }
//
//    /**
//     * Traite le nouveau mot de passe
//     */
//    @PostMapping("/reset-password")
//    public String processResetPassword(@RequestParam String token,
//                                       @RequestParam String password,
//                                       @RequestParam String confirmPassword,
//                                       RedirectAttributes redirectAttributes) {
//        try {
//            // Vérifier que les mots de passe correspondent
//            if (!password.equals(confirmPassword)) {
//                redirectAttributes.addFlashAttribute("error", "Les mots de passe ne correspondent pas.");
//                return "redirect:/reset-password?token=" + token;
//            }
//
//            userService.resetPassword(token, password);
//            redirectAttributes.addFlashAttribute("success", 
//                "Votre mot de passe a été réinitialisé avec succès. Vous pouvez maintenant vous connecter.");
//            return "redirect:/login";
//            
//        } catch (RuntimeException e) {
//            redirectAttributes.addFlashAttribute("error", e.getMessage());
//            return "redirect:/reset-password?token=" + token;
//        }
//    }
}