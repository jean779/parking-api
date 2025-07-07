package com.estapar.parking.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum EventType {
    ENTRY, PARKED, EXIT;

    @JsonCreator
    public static EventType fromString(String value) {
        return EventType.valueOf(value.toUpperCase());
    }
}

