/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.repositories;

import com.securityapp.gofundme.model.Category;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Handy
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Permet de retrouver une catégorie par son nom (ex: Santé, Éducation)
    Optional<Category> findByName(String name);
}