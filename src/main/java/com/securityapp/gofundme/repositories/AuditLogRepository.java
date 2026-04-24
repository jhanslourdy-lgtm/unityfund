/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.repositories;

import com.securityapp.gofundme.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Handy
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}