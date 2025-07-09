package com.example.garage.controller;

import com.example.garage.controller.dto.ErrorResponse;
import com.example.garage.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GarageFullException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleGarageFullException(GarageFullException ex) {
        return new ErrorResponse("GARAGE_FULL", ex.getMessage());
    }

    @ExceptionHandler(CarNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleCarNotFoundException(CarNotFoundException ex) {
        return new ErrorResponse("CAR_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(SpotNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleSpotNotFoundException(SpotNotFoundException ex) {
        return new ErrorResponse("SPOT_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(NoCompatibleSpotFoundException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleNoCompatibleSpotFoundException(NoCompatibleSpotFoundException ex) {
        return new ErrorResponse("NO_COMPATIBLE_SPOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ErrorResponse("INVALID_INPUT", ex.getMessage());
    }
}
