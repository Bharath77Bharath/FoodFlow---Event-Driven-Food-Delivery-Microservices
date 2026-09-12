package com.foodflow.delivery_service.Service;

import com.foodflow.common.Event.PartnerAvailableEvent;
import com.foodflow.delivery_service.Dto.DeliveryPartnerRequestDto;
import com.foodflow.delivery_service.Dto.DeliveryPartnerResponseDto;
import com.foodflow.delivery_service.Entity.DeliveryPartner;
import com.foodflow.delivery_service.Entity.DeliveryPartnerStatus;
import com.foodflow.delivery_service.Exception.DeliveryPartnerNotFoundException;
import com.foodflow.delivery_service.Exception.DuplicateDeliveryPartnerException;
import com.foodflow.delivery_service.Kafka.DeliveryKafkaProducer;
import com.foodflow.delivery_service.Repository.DeliveryPartnerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryPartnerService {

    private final DeliveryPartnerRepo deliveryPartnerRepo;
    private final DeliveryKafkaProducer deliveryKafkaProducer;

    public DeliveryPartnerResponseDto createPartner(DeliveryPartnerRequestDto request) {

        if(deliveryPartnerRepo.existsByPhone(request.getPhone())) {
            throw new DuplicateDeliveryPartnerException("Delivery partner with phone "+request.getPhone()+" already exists!");
        }

        if(deliveryPartnerRepo.existsByVehicleNumber(request.getVehicleNumber())) {
            throw new DuplicateDeliveryPartnerException("Delivery partner with vehicle number "+request.getVehicleNumber()+" already exists!");
        }

        DeliveryPartner partner = convertToDeliveryPartner(request);

        DeliveryPartner savedPartner = deliveryPartnerRepo.save(partner);

        return convertToDeliveryPartnerResponse(savedPartner);
    }

    public List<DeliveryPartnerResponseDto> getAllPartners() {
//        List<DeliveryPartnerResponseDto> responseList = new ArrayList<>();
//        List<DeliveryPartner> partnerList = deliveryPartnerRepo.findAll();
//
//        for(DeliveryPartner partner : partnerList) {
//            responseList.add(convertToDeliveryPartnerResponse(partner));
//        }
//
//        return responseList;

        return deliveryPartnerRepo.findAll().
                stream().
                map(this::convertToDeliveryPartnerResponse).
                toList();
    }

    public DeliveryPartnerResponseDto getPartnerById(Long partnerId) {

        DeliveryPartner partner = deliveryPartnerRepo.findById(partnerId).orElseThrow(() -> new DeliveryPartnerNotFoundException("Delivery partner not found with id: "+partnerId));

        return convertToDeliveryPartnerResponse(partner);
    }

    public DeliveryPartnerResponseDto updatePartner(Long partnerId, DeliveryPartnerRequestDto request) {
        DeliveryPartner partner = deliveryPartnerRepo.findById(partnerId).orElseThrow(() -> new DeliveryPartnerNotFoundException("Delivery partner not found with id: "+partnerId));

        if(!partner.getPhone().equals(request.getPhone()) && deliveryPartnerRepo.existsByPhone(request.getPhone())) {
            throw new DuplicateDeliveryPartnerException("Delivery partner with phone "+request.getPhone()+" already exists!");
        }

        if(!partner.getVehicleNumber().equals(request.getVehicleNumber()) && deliveryPartnerRepo.existsByVehicleNumber(request.getVehicleNumber())) {
            throw new DuplicateDeliveryPartnerException("Delivery partner with vehicle number "+request.getVehicleNumber()+" already exists!");
        }

        partner.setName(request.getName());
        partner.setPhone(request.getPhone());
        partner.setVehicleType(request.getVehicleType());
        partner.setVehicleNumber(request.getVehicleNumber());

        DeliveryPartner savedPartner = deliveryPartnerRepo.save(partner);

        return convertToDeliveryPartnerResponse(savedPartner);
    }

    public DeliveryPartnerResponseDto updatePartnerStatus(Long partnerId, DeliveryPartnerStatus status) {
        DeliveryPartner partner = deliveryPartnerRepo.findById(partnerId).orElseThrow(() -> new DeliveryPartnerNotFoundException("Delivery partner not found with id: "+partnerId));

        partner.setStatus(status);

        if (status == DeliveryPartnerStatus.AVAILABLE) {
            partner.setAvailableSince(LocalDateTime.now());
        } else {
            partner.setAvailableSince(null);
        }

        DeliveryPartner updatedPartner = deliveryPartnerRepo.save(partner);

        if(status == DeliveryPartnerStatus.AVAILABLE) {
            PartnerAvailableEvent availableEvent = new PartnerAvailableEvent(updatedPartner.getId());

            deliveryKafkaProducer.publishPartnerAvailable(availableEvent);
        }

        return convertToDeliveryPartnerResponse(updatedPartner);
    }

    public void deletePartner(Long partnerId) {
        DeliveryPartner partner = deliveryPartnerRepo.findById(partnerId).orElseThrow(() -> new DeliveryPartnerNotFoundException("Delivery partner not found with id: "+partnerId));

        deliveryPartnerRepo.delete(partner);
    }

    //helper methods

    private DeliveryPartnerResponseDto convertToDeliveryPartnerResponse(DeliveryPartner partner) {
        DeliveryPartnerResponseDto response = new DeliveryPartnerResponseDto();

        response.setId(partner.getId());
        response.setName(partner.getName());
        response.setPhone(partner.getPhone());
        response.setVehicleType(partner.getVehicleType());
        response.setVehicleNumber(partner.getVehicleNumber());
        response.setStatus(partner.getStatus());
        response.setCreatedAt(partner.getCreatedAt());
        response.setUpdatedAt(partner.getUpdatedAt());

        return response;
    }

    private DeliveryPartner convertToDeliveryPartner(DeliveryPartnerRequestDto request) {
        DeliveryPartner partner = new DeliveryPartner();

        partner.setName(request.getName());
        partner.setPhone(request.getPhone());
        partner.setVehicleType(request.getVehicleType());
        partner.setVehicleNumber(request.getVehicleNumber());

        return partner;
    }
}
