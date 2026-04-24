/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.repositories;

import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.model.Withdrawal;
import com.securityapp.gofundme.model.WithdrawalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface WithdrawalRepository extends JpaRepository<Withdrawal, Long> {

    List<Withdrawal> findByUserOrderByCreatedAtDesc(User user);

    List<Withdrawal> findByStatusOrderByCreatedAtDesc(WithdrawalStatus status);

    @Query("SELECT COALESCE(SUM(w.amount), 0) FROM Withdrawal w WHERE w.user = :user AND w.status IN ('PENDING', 'PROCESSING', 'COMPLETED')")
    BigDecimal sumWithdrawnByUser(@Param("user") User user);

    boolean existsByUserAndStatus(User user, WithdrawalStatus status);
}