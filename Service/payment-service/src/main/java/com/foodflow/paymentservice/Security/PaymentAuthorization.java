package com.foodflow.paymentservice.Security;

import com.foodflow.paymentservice.Client.OrderServiceClient;
import com.foodflow.paymentservice.Dto.OrderResponseDto;
import com.foodflow.paymentservice.Entity.Payment;
import com.foodflow.paymentservice.Repository.PaymentRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("paymentAuthorization")
@RequiredArgsConstructor
public class PaymentAuthorization {

    private final PaymentRepo paymentRepo;
    private final OrderServiceClient orderServiceClient;

    public boolean isOwner(Long paymentId) {

        Payment payment = paymentRepo.findById(paymentId).orElse(null);

        if (payment == null) {
            return false;
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();

        return currentUserId.equals(payment.getUserId());
    }

    public boolean isOwnerByOrderId(Long orderId) {

        Payment payment =
                paymentRepo.findByOrderId(orderId).orElse(null);

        if (payment == null) {
            return false;
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();

        return currentUserId.equals(payment.getUserId());
    }

    public boolean isOrderOwner(Long orderId) {

        OrderResponseDto order =
                orderServiceClient.getOrderById(orderId);

        Long currentUserId = SecurityUtils.getCurrentUserId();

        return currentUserId.equals(order.getUserId());
    }
}