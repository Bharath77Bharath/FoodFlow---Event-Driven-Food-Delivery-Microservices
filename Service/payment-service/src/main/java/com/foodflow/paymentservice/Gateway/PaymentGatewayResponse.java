package com.foodflow.paymentservice.Gateway;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentGatewayResponse {
    private boolean success;
    private String transactionId;
    private String failureReason;
}
