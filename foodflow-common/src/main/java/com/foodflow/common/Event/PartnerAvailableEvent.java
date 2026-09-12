package com.foodflow.common.Event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartnerAvailableEvent {

    private Long deliveryPartnerId;

}
