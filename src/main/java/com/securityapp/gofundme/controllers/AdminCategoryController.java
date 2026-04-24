package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.Category;
import com.securityapp.gofundme.repositories.CategoryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {
    private final CategoryRepository categoryRepository;

    public AdminCategoryController(CategoryRepository categoryRepository) { this.categoryRepository = categoryRepository; }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("category", new Category());
        return "admin/categories/list";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Category category) {
        categoryRepository.save(category);
        return "redirect:/admin/categories";
    }
}
