package com.foodflow.order_service.Security;

import org.springframework.stereotype.Component;

@Component
public class EventAuthorization {

    public boolean isFrom(String source, String expectedSource) {
        return expectedSource.equals(source);
    }
}