package com.estapar.parking.exception;

public class SectorClosedException extends RuntimeException {
    public SectorClosedException(String message) {
        super(message);
    }
}
