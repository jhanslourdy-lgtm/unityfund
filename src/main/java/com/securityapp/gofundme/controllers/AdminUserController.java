package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.Role;
import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.repositories.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {
    private final UserRepository userRepository;

    public AdminUserController(UserRepository userRepository) { this.userRepository = userRepository; }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users/list";
    }

    @PostMapping("/{id}/role")
    public String changeRole(@PathVariable Long id, @RequestParam Role role) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        user.setRole(role);
        userRepository.save(user);
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/verify")
    public String verify(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        user.setEmailVerified(true);
        userRepository.save(user);
        return "redirect:/admin/users";
    }
}
