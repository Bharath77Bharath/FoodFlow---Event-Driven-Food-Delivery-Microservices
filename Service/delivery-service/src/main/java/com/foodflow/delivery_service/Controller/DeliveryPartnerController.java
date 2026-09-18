package com.foodflow.delivery_service.Controller;

import com.foodflow.delivery_service.Dto.DeliveryPartnerRequestDto;
import com.foodflow.delivery_service.Dto.DeliveryPartnerResponseDto;
import com.foodflow.delivery_service.Entity.DeliveryPartnerStatus;
import com.foodflow.delivery_service.Service.DeliveryPartnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/delivery-partners")
@RequiredArgsConstructor
public class DeliveryPartnerController {

    private final DeliveryPartnerService deliveryPartnerService;

    @PostMapping
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<DeliveryPartnerResponseDto> createPartner(@Valid @RequestBody DeliveryPartnerRequestDto request) {
        DeliveryPartnerResponseDto response = deliveryPartnerService.createPartner(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{partnerId}")
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@deliveryPartnerAuthorization.isOwner(#partnerId)"
    )
    public ResponseEntity<DeliveryPartnerResponseDto> getPartnerById(@PathVariable Long partnerId) {
        DeliveryPartnerResponseDto response = deliveryPartnerService.getPartnerById(partnerId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DeliveryPartnerResponseDto>> getAllPartners() {
        List<DeliveryPartnerResponseDto> responseList = deliveryPartnerService.getAllPartners();

        return new ResponseEntity<>(responseList, HttpStatus.OK);
    }

    @PutMapping("/{partnerId}")
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@deliveryPartnerAuthorization.isOwner(#partnerId)"
    )
    public ResponseEntity<DeliveryPartnerResponseDto> updatePartner(@PathVariable Long partnerId, @Valid @RequestBody DeliveryPartnerRequestDto request) {
        DeliveryPartnerResponseDto response = deliveryPartnerService.updatePartner(partnerId, request);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{partnerId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeliveryPartnerResponseDto> updatePartnerStatus(@PathVariable Long partnerId, @RequestParam DeliveryPartnerStatus status) {
        DeliveryPartnerResponseDto response = deliveryPartnerService.updatePartnerStatus(partnerId,status);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/my-status")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<DeliveryPartnerResponseDto> updateOwnStatus(
            @RequestParam DeliveryPartnerStatus status) {

        DeliveryPartnerResponseDto response =
                deliveryPartnerService.updateOwnStatus(status);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{partnerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeliveryPartnerResponseDto> deletePartner(@PathVariable Long partnerId) {
        deliveryPartnerService.deletePartner(partnerId);

        return ResponseEntity.noContent().build();
    }
}
