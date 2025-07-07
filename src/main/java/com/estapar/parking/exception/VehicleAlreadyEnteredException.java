package com.estapar.parking.exception;

public class VehicleAlreadyEnteredException extends RuntimeException {
    public VehicleAlreadyEnteredException(String message) {
        super(message);
    }
}
