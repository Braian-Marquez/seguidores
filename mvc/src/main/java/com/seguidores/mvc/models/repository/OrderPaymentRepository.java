package com.seguidores.mvc.models.repository;

import com.seguidores.mvc.models.entity.OrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderPaymentRepository extends JpaRepository<OrderPayment,Long> {
    @Query("SELECT o FROM OrderPayment o WHERE o.id=?1")
    Optional<OrderPayment> findById(long orderId);

    @Query("SELECT o FROM OrderPayment o WHERE o.email=?1")
    List<OrderPayment> findByEmail(String email);
    @Query("SELECT o FROM OrderPayment o WHERE o.idService=?1 AND o.email=?2 AND o.link=?3")
    List<OrderPayment> findByExist(Integer idService, String email, String link);
}
