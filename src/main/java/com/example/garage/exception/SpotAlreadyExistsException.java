package com.example.garage.exception;

public class SpotAlreadyExistsException extends RuntimeException {
    public SpotAlreadyExistsException(String message) {
        super(message);
    }
}
