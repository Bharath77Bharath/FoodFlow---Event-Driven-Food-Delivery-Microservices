package com.foodflow.paymentservice.Gateway;

import com.foodflow.paymentservice.Entity.Payment;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public PaymentGatewayResponse processPayment(Payment payment) {

        if(payment.getPaymentMethod().name().equals("CASH")) {
            return new PaymentGatewayResponse(
                    false,
                    null,
                    "Cash payments are not supported by the payment gateway"
            );
        }

        return new PaymentGatewayResponse(
                true,
                "TXN-" + UUID.randomUUID(),
                null
        );
    }
}
