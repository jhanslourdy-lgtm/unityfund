package com.securityapp.gofundme.repositories;

import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.model.Withdrawal;
import com.securityapp.gofundme.model.WithdrawalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@Repository
public interface WithdrawalRepository extends JpaRepository<Withdrawal, Long> {

    List<Withdrawal> findByUserOrderByCreatedAtDesc(User user);

    List<Withdrawal> findByStatusOrderByCreatedAtDesc(WithdrawalStatus status);

    @Query("SELECT COALESCE(SUM(w.amount), 0) FROM Withdrawal w WHERE w.user = :user AND w.status IN :statuses")
    BigDecimal sumWithdrawnByUserAndStatuses(@Param("user") User user, @Param("statuses") Collection<WithdrawalStatus> statuses);

    boolean existsByUserAndStatus(User user, WithdrawalStatus status);
}
