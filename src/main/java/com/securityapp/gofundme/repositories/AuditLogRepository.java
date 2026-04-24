//package com.securityapp.gofundme.repositories;
//
//import com.securityapp.gofundme.model.AuditAction;
//import com.securityapp.gofundme.model.AuditLog;
//import com.securityapp.gofundme.model.AuditStatus;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
//    List<AuditLog> findTop300ByOrderByCreatedAtDesc();
//    List<AuditLog> findTop300ByActorEmailContainingIgnoreCaseOrderByCreatedAtDesc(String actorEmail);
//    List<AuditLog> findTop300ByActionOrderByCreatedAtDesc(AuditAction action);
//    List<AuditLog> findTop300ByStatusOrderByCreatedAtDesc(AuditStatus status);
//    List<AuditLog> findTop300ByEntityNameOrderByCreatedAtDesc(String entityName);
//    List<AuditLog> findTop200ByOrderByCreatedAtDesc();
//}
package com.securityapp.gofundme.repositories;

import com.securityapp.gofundme.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findTop200ByOrderByCreatedAtDesc();
}