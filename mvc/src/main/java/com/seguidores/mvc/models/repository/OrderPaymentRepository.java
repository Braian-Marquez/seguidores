package com.seguidores.mvc.models.repository;

import com.seguidores.mvc.models.entity.OrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OrderPaymentRepository extends JpaRepository<OrderPayment,Long> {
    @Query("SELECT o FROM OrderPayment o WHERE o.orderId=?1 OR o.email=?2")
    Optional<OrderPayment> findByIdAndEmail(long orderId, String email);
}
