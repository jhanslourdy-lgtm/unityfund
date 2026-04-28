package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class CompleteProfileController {

    private final UserRepository userRepository;

    public CompleteProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/complete-profile")
    public String completeProfileForm(Authentication authentication, Model model) {
        User user = getAuthenticatedUser(authentication);

        if (user.isProfileCompleted()) {
            return "redirect:/home";
        }

        model.addAttribute("user", user);
        return "complete-profile";
    }

    @PostMapping("/complete-profile")
    public String completeProfileSubmit(
            Authentication authentication,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String website,
            @RequestParam(required = false) String bio,
            HttpServletRequest request
    ) {
        User user = getAuthenticatedUser(authentication);

        user.setPhone(clean(phone));
        user.setCountry(clean(country));
        user.setWebsite(clean(website));
        user.setBio(clean(bio));
        user.setProfileCompleted(true);

        userRepository.save(user);

        if (user.getRole().name().equals("ROLE_ADMIN")) {
            return "redirect:/admin/dashboard";
        }

        return "redirect:/home";
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non connecté");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
