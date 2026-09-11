package com.foodflow.common.Event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCreatedEvent {

    private Long paymentId;
    private Long orderId;
    private Long userId;
    private BigDecimal totalAmount;

}
