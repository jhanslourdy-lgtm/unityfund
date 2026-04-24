/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.Role;
import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.repositories.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author Handy
 */
@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserRepository userRepository;

    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users/list";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/make-admin/{id}")
    public String makeAdmin(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setRole(Role.ROLE_ADMIN);
        userRepository.save(user);
        return "redirect:/admin/users";
    }

    @GetMapping("/make-user/{id}")
    public String makeUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);
        return "redirect:/admin/users";
    }
}