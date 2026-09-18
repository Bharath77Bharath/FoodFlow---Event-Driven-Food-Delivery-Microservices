package com.foodflow.order_service.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFound(UserNotFoundException e) {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                e.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RestaurantNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleRestaurantNotFound(RestaurantNotFoundException e) {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                e.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MenuItemNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleMenuItemNotFound(MenuItemNotFoundException e) {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                e.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponseDto> handleServiceUnavailable(ServiceUnavailableException e) {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                e.getMessage(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(responseDto, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(MenuItemUnavailableException.class)
    public ResponseEntity<ErrorResponseDto> handleMenuItemUnavailable(MenuItemUnavailableException e) {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                e.getMessage(),
                HttpStatus.CONFLICT.value(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(responseDto, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidOrderException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidOrderException(InvalidOrderException e) {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleOrderNotFoundException(OrderNotFoundException e) {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                e.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidOrderStatusException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidOrderStatus(InvalidOrderStatusException e) {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(Exception ex) {

        ex.printStackTrace();

        ErrorResponseDto error = new ErrorResponseDto(
                "An unexpected error occurred: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }

}
