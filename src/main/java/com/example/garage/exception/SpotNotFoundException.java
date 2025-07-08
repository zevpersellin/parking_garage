package com.example.garage.exception;

public class SpotNotFoundException extends RuntimeException {
    public SpotNotFoundException(String message) {
        super(message);
    }
}
