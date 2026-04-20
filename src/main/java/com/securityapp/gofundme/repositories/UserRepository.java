/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.repositories;

import com.securityapp.gofundme.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Handy
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data JPA génère la requête SQL automatiquement (Protection contre injection SQL)
    Optional<User> findByEmail(String email);
    // Pour vérifier si un compte existe déjà lors de l'inscription
    Boolean existsByEmail(String email);
       Optional<User> findByResetToken(String resetToken);
}
