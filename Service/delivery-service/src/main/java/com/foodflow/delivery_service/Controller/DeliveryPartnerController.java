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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/delivery-partners")
@RequiredArgsConstructor
public class DeliveryPartnerController {

    private final DeliveryPartnerService deliveryPartnerService;

    @PostMapping
    public ResponseEntity<DeliveryPartnerResponseDto> createPartner(@Valid @RequestBody DeliveryPartnerRequestDto request) {
        DeliveryPartnerResponseDto response = deliveryPartnerService.createPartner(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{partnerId}")
    public ResponseEntity<DeliveryPartnerResponseDto> getPartnerById(@PathVariable Long partnerId) {
        DeliveryPartnerResponseDto response = deliveryPartnerService.getPartnerById(partnerId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<DeliveryPartnerResponseDto>> getAllPartners() {
        List<DeliveryPartnerResponseDto> responseList = deliveryPartnerService.getAllPartners();

        return new ResponseEntity<>(responseList, HttpStatus.OK);
    }

    @PutMapping("/{partnerId}")
    public ResponseEntity<DeliveryPartnerResponseDto> updatePartner(@PathVariable Long partnerId, @Valid @RequestBody DeliveryPartnerRequestDto request) {
        DeliveryPartnerResponseDto response = deliveryPartnerService.updatePartner(partnerId, request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{partnerId}/status")
    public ResponseEntity<DeliveryPartnerResponseDto> updatePartnerStatus(@PathVariable Long partnerId, @RequestParam DeliveryPartnerStatus status) {
        DeliveryPartnerResponseDto response = deliveryPartnerService.updatePartnerStatus(partnerId,status);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{partnerId}")
    public ResponseEntity<DeliveryPartnerResponseDto> deletePartner(@PathVariable Long partnerId) {
        deliveryPartnerService.deletePartner(partnerId);

        return ResponseEntity.noContent().build();
    }
}
