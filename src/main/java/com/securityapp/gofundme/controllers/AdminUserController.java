package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.AuditAction;
import com.securityapp.gofundme.model.AuditStatus;
import com.securityapp.gofundme.model.Role;
import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.repositories.UserRepository;
import com.securityapp.gofundme.services.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public AdminUserController(UserRepository userRepository, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users/list";
    }

    @PostMapping("/{id}/role")
    public String changeRole(@PathVariable Long id, @RequestParam Role role, HttpServletRequest request) {
        updateRole(id, role, request);
        return "redirect:/admin/users";
    }

    @GetMapping("/make-admin/{id}")
    public String makeAdmin(@PathVariable Long id, HttpServletRequest request) {
        updateRole(id, Role.ROLE_ADMIN, request);
        return "redirect:/admin/users";
    }

    @GetMapping("/make-user/{id}")
    public String makeUser(@PathVariable Long id, HttpServletRequest request) {
        updateRole(id, Role.ROLE_USER, request);
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/verify")
    public String verifyPost(@PathVariable Long id, HttpServletRequest request) {
        verifyUser(id, request);
        return "redirect:/admin/users";
    }

    @GetMapping("/verify/{id}")
    public String verifyGet(@PathVariable Long id, HttpServletRequest request) {
        verifyUser(id, request);
        return "redirect:/admin/users";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, HttpServletRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        String oldValue = "email=" + user.getEmail() + ", verified=" + user.isEmailVerified() + ", role=" + user.getRole();
        // Suppression logique : évite les erreurs de clés étrangères avec campagnes, dons, retraits, commentaires.
        user.setEmailVerified(false);
        userRepository.save(user);
        auditLogService.log(AuditAction.ADMIN_ACTION, AuditStatus.SUCCESS, "User", user.getId(),
                "Utilisateur désactivé par l'admin", oldValue,
                "email=" + user.getEmail() + ", verified=" + user.isEmailVerified() + ", role=" + user.getRole(), null, request);
        return "redirect:/admin/users";
    }

    private void updateRole(Long id, Role role, HttpServletRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        Role oldRole = user.getRole();
        user.setRole(role);
        userRepository.save(user);
        auditLogService.log(AuditAction.ADMIN_ACTION, AuditStatus.SUCCESS, "User", user.getId(),
                "Changement de rôle utilisateur", "role=" + oldRole, "role=" + role, null, request);
    }

    private void verifyUser(Long id, HttpServletRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        boolean old = user.isEmailVerified();
        user.setEmailVerified(true);
        userRepository.save(user);
        auditLogService.log(AuditAction.ADMIN_ACTION, AuditStatus.SUCCESS, "User", user.getId(),
                "Validation email utilisateur par l'admin", "emailVerified=" + old, "emailVerified=true", null, request);
    }
}
