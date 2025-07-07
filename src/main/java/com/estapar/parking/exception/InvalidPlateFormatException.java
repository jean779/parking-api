package com.estapar.parking.exception;

public class InvalidPlateFormatException extends RuntimeException {
    public InvalidPlateFormatException(String message) {
        super(message);
    }
}
