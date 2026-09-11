package com.foodflow.common.Event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventEnvelope {

    private String eventId;

    private String eventType;

    private LocalDateTime timestamp;

    private String source;

    private Object data;
}
