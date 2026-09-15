package com.foodflow.inventory_service.Repository;


import com.foodflow.inventory_service.Entity.InventoryReservation;
import com.foodflow.inventory_service.Entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryReservationRepo extends JpaRepository<InventoryReservation, Long> {

    Optional<InventoryReservation> findByOrderIdAndMenuItemId(
            Long orderId,
            Long menuItemId
    );

    List<InventoryReservation> findByOrderIdAndStatus(
            Long orderId,
            ReservationStatus status
    );
}