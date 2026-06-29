package com.example.badwallet_api.exception;

public class FactureNotFoundException extends RuntimeException {
    public FactureNotFoundException(String message) {
        super(message);
    }
}
