package com.example.garage.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class NoCompatibleSpotFoundException extends RuntimeException {
    public NoCompatibleSpotFoundException(String message) {
        super(message);
    }
}
