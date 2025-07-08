package com.example.garage.exception;

public class CarNotFoundException extends RuntimeException {
    public CarNotFoundException(String message) {
        super(message);
    }
}
