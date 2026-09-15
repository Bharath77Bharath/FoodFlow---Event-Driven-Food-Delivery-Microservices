package com.foodflow.inventory_service.Service;

import com.foodflow.common.Event.OrderCreatedEvent;
import com.foodflow.common.Event.OrderItemEvent;
import com.foodflow.inventory_service.Client.RestaurantServiceClient;
import com.foodflow.inventory_service.Dto.CreateInventoryRequest;
import com.foodflow.inventory_service.Dto.InventoryAvailabilityRequest;
import com.foodflow.inventory_service.Dto.InventoryResponse;
import com.foodflow.inventory_service.Dto.UpdateInventoryRequest;
import com.foodflow.inventory_service.Entity.Inventory;
import com.foodflow.inventory_service.Entity.InventoryReservation;
import com.foodflow.inventory_service.Entity.ReservationStatus;
import com.foodflow.inventory_service.Exception.*;
import com.foodflow.inventory_service.Repository.InventoryRepo;
import com.foodflow.inventory_service.Repository.InventoryReservationRepo;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

    private final InventoryRepo inventoryRepo;
    private final RestaurantServiceClient restaurantServiceClient;
    private final InventoryReservationRepo inventoryReservationRepo;

    public InventoryResponse createInventory(CreateInventoryRequest request) {

        try {
            restaurantServiceClient.getMenuItemById(request.getMenuItemId());
        } catch (FeignException.NotFound e) {
            throw new MenuItemNotFoundException("Menu item not found with id: "+request.getMenuItemId());
        }

        if(inventoryRepo.existsByMenuItemId(request.getMenuItemId())) {
            throw new   DuplicateInventoryException("Inventory already exists for menu item with id: " + request.getMenuItemId());
        }

        Inventory inventory = convertToInventory(request);

        Inventory savedInventory = inventoryRepo.save(inventory);

        return convertInventorytoInventoryResponse(savedInventory);
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventory(Long inventoryId) {

        Inventory inventory = inventoryRepo.findById(inventoryId)
                .orElseThrow(() ->
                        new InventoryNotFoundException(
                                "Inventory not found with id: " + inventoryId
                        )
                );

        return convertInventorytoInventoryResponse(inventory);
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByMenuItemId(Long menuItemId) {

        Inventory inventory = inventoryRepo
                .findByMenuItemId(menuItemId)
                .orElseThrow(() ->
                        new InventoryNotFoundException(
                                "Inventory not found for menu item: " + menuItemId
                        )
                );

        return convertInventorytoInventoryResponse(inventory);
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getAllInventory() {

        return inventoryRepo.findAll()
                .stream()
                .map(this::convertInventorytoInventoryResponse)
                .toList();
    }

    public InventoryResponse updateInventory(Long inventoryId, UpdateInventoryRequest request) {

        Inventory inventory = inventoryRepo.findById(inventoryId)
                .orElseThrow(() ->
                        new InventoryNotFoundException(
                                "Inventory not found with id: " + inventoryId
                        )
                );

        inventory.setAvailableQuantity(request.getQuantity());

        return convertInventorytoInventoryResponse(inventoryRepo.save(inventory));
    }

    public void deleteInventory(Long inventoryId) {

        Inventory inventory = inventoryRepo.findById(inventoryId)
                .orElseThrow(() ->
                        new InventoryNotFoundException(
                                "Inventory not found with id: " + inventoryId
                        )
                );

        inventoryRepo.delete(inventory);
    }

    public InventoryResponse reserveStock(Long orderId, Long menuItemId, Integer quantity) {

        validateQuantity(quantity);

        // Check whether this reservation already exists
        InventoryReservation existingReservation =
                inventoryReservationRepo
                        .findByOrderIdAndMenuItemId(orderId, menuItemId)
                        .orElse(null);

        if (existingReservation != null) {

            if (existingReservation.getStatus()
                    == ReservationStatus.RESERVED) {

                // Already reserved - idempotent request
                Inventory inventory = inventoryRepo
                        .findByMenuItemId(menuItemId)
                        .orElseThrow(() ->
                                new InventoryNotFoundException(
                                        "Inventory not found for menu item: "
                                                + menuItemId
                                )
                        );

                return convertInventorytoInventoryResponse(inventory);
            }

            if (existingReservation.getStatus()
                    == ReservationStatus.RELEASED) {

                throw new InvalidReservationException(
                        "Reservation was already released for order: "
                                + orderId
                );
            }
        }

        // Find inventory
        Inventory inventory = inventoryRepo
                .findByMenuItemId(menuItemId)
                .orElseThrow(() ->
                        new InventoryNotFoundException(
                                "Inventory not found for menu item: "
                                        + menuItemId
                        )
                );

        // Check stock
        if (inventory.getAvailableQuantity() < quantity) {

            throw new InsufficientInventoryException(
                    "Insufficient inventory for menu item: "
                            + menuItemId
            );
        }

        // Update inventory
        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity() - quantity
        );

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() + quantity
        );

        inventoryRepo.save(inventory);

        // Create reservation
        InventoryReservation reservation =
                InventoryReservation.builder()
                        .orderId(orderId)
                        .menuItemId(menuItemId)
                        .quantity(quantity)
                        .status(ReservationStatus.RESERVED)
                        .build();

        inventoryReservationRepo.save(reservation);

        return convertInventorytoInventoryResponse(inventory);
    }

    public InventoryResponse releaseStock(Long orderId, Long menuItemId) {

        InventoryReservation reservation =
                inventoryReservationRepo
                        .findByOrderIdAndMenuItemId(orderId, menuItemId)
                        .orElseThrow(() ->
                                new InvalidInventoryReleaseException(
                                        "Reservation not found for order: "
                                                + orderId
                                )
                        );

        if (reservation.getStatus()
                == ReservationStatus.RELEASED) {

            // Already released - idempotent
            return getInventoryByMenuItemId(menuItemId);
        }

        Inventory inventory = inventoryRepo
                .findByMenuItemId(menuItemId)
                .orElseThrow(() ->
                        new InventoryNotFoundException(
                                "Inventory not found for menu item: "
                                        + menuItemId
                        )
                );

        Integer quantity = reservation.getQuantity();

        // Move reserved stock back to available
        inventory.setReservedQuantity(
                inventory.getReservedQuantity() - quantity
        );

        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity() + quantity
        );

        inventoryRepo.save(inventory);

        // Mark reservation as released
        reservation.setStatus(ReservationStatus.RELEASED);

        inventoryReservationRepo.save(reservation);

        return convertInventorytoInventoryResponse(inventory);
    }

    @Transactional
    public void reserveOrder(OrderCreatedEvent orderCreatedEvent) {

        Long orderId = orderCreatedEvent.getOrderId();

        List<OrderItemEvent> items = orderCreatedEvent.getItems();

        if (items == null || items.isEmpty()) {
            throw new InvalidReservationException(
                    "Order must contain at least one item"
            );
        }

        /*
         * Step 1:
         * Check idempotency.
         *
         * If all reservations already exist as RESERVED,
         * this is a duplicate Kafka event.
         */
        boolean allAlreadyReserved = true;

        for (OrderItemEvent item : items) {

            Optional<InventoryReservation> existingReservation =
                    inventoryReservationRepo.findByOrderIdAndMenuItemId(
                            orderId,
                            item.getMenuItemId()
                    );

            if (existingReservation.isEmpty()) {
                allAlreadyReserved = false;
                continue;
            }

            if (existingReservation.get().getStatus() == ReservationStatus.RELEASED) {
                throw new InvalidReservationException(
                        "Reservation was already released for order "
                                + orderId
                                + " and menu item "
                                + item.getMenuItemId()
                );
            }
        }

        if (allAlreadyReserved) {
            return;
        }

        /*
         * Step 2:
         * Detect an inconsistent partial reservation state.
         *
         * We don't want to silently create only the missing
         * reservations.
         */
        for (OrderItemEvent item : items) {

            Optional<InventoryReservation> existingReservation =
                    inventoryReservationRepo.findByOrderIdAndMenuItemId(
                            orderId,
                            item.getMenuItemId()
                    );

            if (existingReservation.isPresent()
                    && existingReservation.get().getStatus() == ReservationStatus.RESERVED) {

                throw new InvalidReservationException(
                        "Partial reservation already exists for order "
                                + orderId
                );
            }
        }

        /*
         * Step 3:
         * Validate ALL inventory before modifying anything.
         */
        List<Inventory> inventories = new ArrayList<>();

        for (OrderItemEvent item : items) {

            Inventory inventory = inventoryRepo
                    .findByMenuItemId(item.getMenuItemId())
                    .orElseThrow(() ->
                            new InventoryNotFoundException(
                                    "Inventory not found for menu item "
                                            + item.getMenuItemId()
                            )
                    );

            if (inventory.getAvailableQuantity() < item.getQuantity()) {
                throw new InsufficientInventoryException(
                        "Insufficient inventory for menu item "
                                + item.getMenuItemId()
                );
            }

            inventories.add(inventory);
        }

        /*
         * Step 4:
         * Only after ALL checks pass, modify inventory.
         */
        for (int i = 0; i < items.size(); i++) {

            OrderItemEvent item = items.get(i);
            Inventory inventory = inventories.get(i);

            inventory.setAvailableQuantity(
                    inventory.getAvailableQuantity() - item.getQuantity()
            );

            inventory.setReservedQuantity(
                    inventory.getReservedQuantity() + item.getQuantity()
            );

            inventoryRepo.save(inventory);

            InventoryReservation reservation =
                    InventoryReservation.builder()
                            .orderId(orderId)
                            .menuItemId(item.getMenuItemId())
                            .quantity(item.getQuantity())
                            .status(ReservationStatus.RESERVED)
                            .build();

            inventoryReservationRepo.save(reservation);
        }
    }

    @Transactional(readOnly = true)
    public void checkAvailability(InventoryAvailabilityRequest request) {

        List<OrderItemEvent> items = request.getItems();

        if (items == null || items.isEmpty()) {
            throw new InvalidReservationException(
                    "Order must contain at least one item"
            );
        }

        for (OrderItemEvent item : items) {

            Inventory inventory = inventoryRepo
                    .findByMenuItemId(item.getMenuItemId())
                    .orElseThrow(() ->
                            new InventoryNotFoundException(
                                    "Inventory not found for menu item "
                                            + item.getMenuItemId()
                            )
                    );

            if (inventory.getAvailableQuantity() < item.getQuantity()) {

                throw new InsufficientInventoryException(
                        "Insufficient inventory for menu item "
                                + item.getMenuItemId()
                                + ". Available: "
                                + inventory.getAvailableQuantity()
                                + ", Requested: "
                                + item.getQuantity()
                );
            }
        }
    }

    @Transactional
    public List<OrderItemEvent> releaseReservations(Long orderId) {

        List<InventoryReservation> reservations =
                inventoryReservationRepo.findByOrderIdAndStatus(
                        orderId,
                        ReservationStatus.RESERVED
                );

        List<OrderItemEvent> releasedItems = new ArrayList<>();

        for (InventoryReservation reservation : reservations) {

            Inventory inventory = inventoryRepo
                    .findByMenuItemId(reservation.getMenuItemId())
                    .orElseThrow(() ->
                            new InventoryNotFoundException(
                                    "Inventory not found for menu item "
                                            + reservation.getMenuItemId()
                            )
                    );

            inventory.setReservedQuantity(
                    inventory.getReservedQuantity()
                            - reservation.getQuantity()
            );

            inventory.setAvailableQuantity(
                    inventory.getAvailableQuantity()
                            + reservation.getQuantity()
            );

            inventoryRepo.save(inventory);

            reservation.setStatus(ReservationStatus.RELEASED);

            inventoryReservationRepo.save(reservation);

            releasedItems.add(
                    new OrderItemEvent(
                            reservation.getMenuItemId(),
                            reservation.getQuantity()
                    )
            );
        }

        return releasedItems;
    }


    //helper methods
    private InventoryResponse convertInventorytoInventoryResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .menuItemId(inventory.getMenuItemId())
                .menuItemName(inventory.getMenuItemName())
                .availableQuantity(inventory.getAvailableQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }

    private Inventory convertToInventory(CreateInventoryRequest request) {
        return Inventory.builder()
                .menuItemId(request.getMenuItemId())
                .menuItemName(restaurantServiceClient.getMenuItemById(request.getMenuItemId()).getName())
                .availableQuantity(request.getQuantity())
                .reservedQuantity(0)
                .build();
    }

    private void validateQuantity(Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }
    }
}
