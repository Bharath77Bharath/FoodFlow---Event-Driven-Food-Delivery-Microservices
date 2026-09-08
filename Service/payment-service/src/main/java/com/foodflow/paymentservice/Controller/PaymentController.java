package com.foodflow.paymentservice.Controller;

import com.foodflow.paymentservice.Dto.PaymentRequest;
import com.foodflow.paymentservice.Dto.PaymentResponse;
import com.foodflow.paymentservice.Service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("{paymentId}/process")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @PathVariable Long paymentId) {
        PaymentResponse response = paymentService.processPayment(paymentId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(@Valid @PathVariable Long paymentId) {
        PaymentResponse response = paymentService.getPaymentById(paymentId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@Valid @PathVariable Long orderId) {
        PaymentResponse response = paymentService.getPaymentByOrderId(orderId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
