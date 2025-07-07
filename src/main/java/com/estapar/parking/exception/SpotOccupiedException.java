package com.estapar.parking.exception;

public class SpotOccupiedException extends RuntimeException {
    public SpotOccupiedException(String message) {
        super(message);
    }
}
