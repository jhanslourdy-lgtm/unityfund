//package com.securityapp.gofundme.controllers;
//
//import com.securityapp.gofundme.model.User;
//import com.securityapp.gofundme.services.UserService;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//@Controller
//public class AuthController {
//
//    @Autowired
//    private UserService userService;
//
//    @GetMapping("/login")
//    public String login() {
//        return "login";
//    }
//
//    @GetMapping("/register")
//    public String showRegistrationForm(Model model) {
//        model.addAttribute("user", new User());
//        return "register";
//    }
//
//    @PostMapping("/register")
//    public String registerUser(@Valid @ModelAttribute("user") User user, 
//                               BindingResult result,
//                               RedirectAttributes redirectAttributes) {
//        if (result.hasErrors()) {
//            return "register";
//        }
//        try {
//            userService.registerNewUser(user);
//            return "redirect:/verify?email=" + user.getEmail();
//        } catch (RuntimeException e) {
//            result.rejectValue("email", "error.user", e.getMessage());
//            return "register";
//        }
//    }
//
//    // Page de vérification du code
//    @GetMapping("/verify")
//    public String showVerificationPage(@RequestParam String email, Model model) {
//        model.addAttribute("email", email);
//        return "verify";
//    }
//
//    @PostMapping("/verify")
//    public String verifyEmail(@RequestParam String email, 
//                              @RequestParam String code,
//                              RedirectAttributes redirectAttributes) {
//        try {
//            userService.verifyEmail(email, code);
//            redirectAttributes.addFlashAttribute("verified", true);
//            return "redirect:/login";
//        } catch (RuntimeException e) {
//            redirectAttributes.addFlashAttribute("error", e.getMessage());
//            return "redirect:/verify?email=" + email;
//        }
//    }
//}
package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, 
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "register";
        }
        try {
            userService.registerNewUser(user);
            return "redirect:/verify?email=" + user.getEmail();
        } catch (RuntimeException e) {
            result.rejectValue("email", "error.user", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/verify")
    public String showVerificationPage(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "verify";
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

    // ========== NOUVEAUX ENDPOINTS POUR MOT DE PASSE OUBLIÉ ==========

    /**
     * Affiche le formulaire "Mot de passe oublié"
     */
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    /**
     * Traite la demande de reset et envoie l'email
     */
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email,
                                        RedirectAttributes redirectAttributes) {
        try {
            userService.requestPasswordReset(email);
            // Message générique pour ne pas révéler si l'email existe
            redirectAttributes.addFlashAttribute("success", 
                "Si un compte existe avec cet email, un lien de réinitialisation a été envoyé.");
            return "redirect:/forgot-password";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Une erreur est survenue. Veuillez réessayer.");
            return "redirect:/forgot-password";
        }
    }

    /**
     * Affiche le formulaire de nouveau mot de passe (depuis le lien email)
     */
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String token, Model model) {
        try {
            User user = userService.validateResetToken(token);
            model.addAttribute("token", token);
            model.addAttribute("email", user.getEmail());
            return "reset-password";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("invalidToken", true);
            return "reset-password";
        }
    }

    /**
     * Traite le nouveau mot de passe
     */
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String token,
                                       @RequestParam String password,
                                       @RequestParam String confirmPassword,
                                       RedirectAttributes redirectAttributes) {
        try {
            // Vérifier que les mots de passe correspondent
            if (!password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Les mots de passe ne correspondent pas.");
                return "redirect:/reset-password?token=" + token;
            }

            userService.resetPassword(token, password);
            redirectAttributes.addFlashAttribute("success", 
                "Votre mot de passe a été réinitialisé avec succès. Vous pouvez maintenant vous connecter.");
            return "redirect:/login";
            
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reset-password?token=" + token;
        }
    }
}