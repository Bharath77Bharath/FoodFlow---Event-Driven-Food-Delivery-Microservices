package com.foodflow.paymentservice.Gateway;


import com.foodflow.paymentservice.Entity.Payment;

public interface PaymentGateway {

    public PaymentGatewayResponse processPayment(Payment payment);

}
