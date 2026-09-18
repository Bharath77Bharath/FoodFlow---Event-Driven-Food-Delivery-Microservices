package com.foodflow.delivery_service.Repository;

import com.foodflow.delivery_service.Entity.DeliveryPartner;
import com.foodflow.delivery_service.Entity.DeliveryPartnerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryPartnerRepo extends JpaRepository<DeliveryPartner, Long> {

    Optional<DeliveryPartner> findByPhone(String phone);

    Optional<DeliveryPartner> findByVehicleNumber(String vehicleNumber);

    Optional<DeliveryPartner> findByUserId(Long userId);

    List<DeliveryPartner> findByStatus(DeliveryPartnerStatus status);

    boolean existsByPhone(String phone);

    boolean existsByVehicleNumber(String vehicleNumber);

    Optional<DeliveryPartner> findFirstByStatusOrderByAvailableSinceAsc(DeliveryPartnerStatus status);
}
