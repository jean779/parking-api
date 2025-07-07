package com.estapar.parking.util.date;

import com.estapar.parking.exception.InvalidDateFormatException;

import java.time.LocalDateTime;

public class DateValidator {

    public static LocalDateTime parseOrThrow(String dateString) {
        try {
            return TimeUtils.parseDateTime(dateString);
        } catch (Exception ex) {
            throw new InvalidDateFormatException("Invalid data format: " + dateString);
        }
    }
}
