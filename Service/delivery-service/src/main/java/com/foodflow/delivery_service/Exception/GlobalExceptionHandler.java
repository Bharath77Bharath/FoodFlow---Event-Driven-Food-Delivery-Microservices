package com.foodflow.delivery_service.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DeliveryPartnerNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleDeliveryPartnerNotFound(DeliveryPartnerNotFoundException e) {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                e.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DeliveryNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleDeliveryNotFound(DeliveryNotFoundException e) {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                e.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateDeliveryPartnerException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicateDeliveryPartner(DuplicateDeliveryPartnerException e) {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                e.getMessage(),
                HttpStatus.CONFLICT.value(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(responseDto, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidDeliveryStatusException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidDeliveryStatus(InvalidDeliveryStatusException e) {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoAvailablePartnerException.class)
    public ResponseEntity<ErrorResponseDto> handleNoAvailablePartnerException(NoAvailablePartnerException e) {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                e.getMessage(),
                HttpStatus.CONFLICT.value(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(responseDto, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DuplicateDeliveryException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicateDelivery(DuplicateDeliveryException e) {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                e.getMessage(),
                HttpStatus.CONFLICT.value(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(responseDto, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().getFirst().getDefaultMessage();
        ErrorResponseDto responseDto = new ErrorResponseDto(
                message,
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
    }

}
