package com.foodflow.inventory_service.Repository;

import com.foodflow.inventory_service.Entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepo extends JpaRepository<Inventory, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i where i.menuItemId = :menuItemId")
    Optional<Inventory> findByMenuItemIdForUpdate(@Param("menuItemId") Long menuItemId);

    Optional<Inventory> findByMenuItemId(Long menuItemId);

    boolean existsByMenuItemId(Long menuItemId);
}
