package com.foodflow.delivery_service.Exception;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ErrorResponseDto {

    private String message;
    private int status;
    private LocalDateTime timestamp;

}
