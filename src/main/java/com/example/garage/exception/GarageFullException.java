package com.example.garage.exception;

public class GarageFullException extends RuntimeException {
    public GarageFullException(String message) {
        super(message);
    }
}
