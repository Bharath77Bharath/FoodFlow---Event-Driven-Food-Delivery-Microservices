package com.foodflow.delivery_service.Repository;

import com.foodflow.delivery_service.Entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryRepo extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);
}
    