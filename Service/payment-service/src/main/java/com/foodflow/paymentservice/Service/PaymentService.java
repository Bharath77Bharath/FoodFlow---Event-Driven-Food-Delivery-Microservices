package com.foodflow.paymentservice.Service;

import com.foodflow.paymentservice.Client.OrderServiceClient;
import com.foodflow.paymentservice.Dto.OrderResponseDto;
import com.foodflow.paymentservice.Dto.PaymentRequest;
import com.foodflow.paymentservice.Dto.PaymentResponse;
import com.foodflow.paymentservice.Entity.Payment;
import com.foodflow.paymentservice.Entity.PaymentStatus;
import com.foodflow.common.Event.*;
import com.foodflow.paymentservice.Exception.*;
import com.foodflow.paymentservice.Gateway.PaymentGateway;
import com.foodflow.paymentservice.Gateway.PaymentGatewayResponse;
import com.foodflow.paymentservice.Kafka.PaymentKafkaProducer;
import com.foodflow.paymentservice.Repository.PaymentRepo;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepo paymentRepo;
    private final OrderServiceClient orderServiceClient;
    private final PaymentGateway paymentGateway;
    private final PaymentKafkaProducer paymentKafkaProducer;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {

        OrderResponseDto responseDto;
        try {
            responseDto = orderServiceClient.getOrderById(request.getOrderId());
        }catch (FeignException.NotFound e) {
            throw new OrderNotFoundException("Order not found with id: "+request.getOrderId());
        }

        if(!"PLACED".equals(responseDto.getStatus())) {
            throw new InvalidOrderStatusException("Payment can only be initiated for PLACED orders");
        }

        if(paymentRepo.existsByOrderId(request.getOrderId())) {
            throw new PaymentAlreadyExistsException("Payment already exists with id: "+request.getOrderId());
        }

        Payment payment = convertToPayment(request, responseDto);

        Payment savedPayment = paymentRepo.save(payment);

        PaymentCreatedEvent event = new PaymentCreatedEvent(
                savedPayment.getId(),
                savedPayment.getOrderId(),
                savedPayment.getUserId(),
                savedPayment.getAmount()
        );

        paymentKafkaProducer.publishPaymentCreated(event);

        return convertToPaymentResponse(savedPayment);
    }

    @Transactional
    public PaymentResponse processPayment(Long paymentId) {
        Payment payment = paymentRepo.findById(paymentId).orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: "+paymentId));

        if(payment.getStatus() != PaymentStatus.PENDING) {
            throw new PaymentAlreadyProcessedException("Payment is already processed");
        }

        PaymentGatewayResponse paymentResponse = paymentGateway.processPayment(payment);

        if(paymentResponse.isSuccess()) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(paymentResponse.getTransactionId());
            payment.setFailureReason(null);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setTransactionId(null);
            payment.setFailureReason(paymentResponse.getFailureReason());
        }

        Payment updatedPayment = paymentRepo.save(payment);

        if(updatedPayment.getStatus() == PaymentStatus.SUCCESS) {
            PaymentSuccessEvent event = new PaymentSuccessEvent(
                    updatedPayment.getId(),
                    updatedPayment.getOrderId(),
                    updatedPayment.getUserId(),
                    updatedPayment.getAmount()
            );

            paymentKafkaProducer.publishPaymentSuccess(event);
        }

        if(updatedPayment.getStatus() == PaymentStatus.FAILED) {
            PaymentFailedEvent event = new PaymentFailedEvent(
                    updatedPayment.getOrderId(),
                    updatedPayment.getUserId(),
                    updatedPayment.getFailureReason()
            );

            paymentKafkaProducer.publishPaymentFailed(event);
        }

        return convertToPaymentResponse(updatedPayment);

    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepo.findById(paymentId).orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: "+paymentId));

        return convertToPaymentResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepo.findByOrderId(orderId).orElseThrow(() -> new PaymentNotFoundException("Payment not found with orderId: "+orderId));

        return convertToPaymentResponse(payment);
    }

    //helper methods
    private PaymentResponse convertToPaymentResponse(Payment payment) {
        PaymentResponse paymentResponse = new PaymentResponse();

        paymentResponse.setId(payment.getId());
        paymentResponse.setUserId(payment.getUserId());
        paymentResponse.setOrderId(payment.getOrderId());
        paymentResponse.setAmount(payment.getAmount());
        paymentResponse.setPaymentMethod(payment.getPaymentMethod());
        paymentResponse.setStatus(payment.getStatus());
        paymentResponse.setTransactionId(payment.getTransactionId());
        paymentResponse.setFailureReason(payment.getFailureReason());
        paymentResponse.setCreatedAt(payment.getCreatedAt());
        paymentResponse.setUpdatedAt(payment.getUpdatedAt());

        return paymentResponse;
    }

    private Payment convertToPayment(PaymentRequest request, OrderResponseDto order) {
        Payment payment = new Payment();

        payment.setOrderId(order.getId());
        payment.setUserId(order.getUserId());
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(PaymentStatus.PENDING);
        return payment;
    }
}
