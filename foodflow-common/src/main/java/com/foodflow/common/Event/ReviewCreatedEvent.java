package com.foodflow.common.Event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewCreatedEvent {

    private Long reviewId;
    private Long orderId;
    private Long userId;
    private Long restaurantId;
    private Integer rating;

}
