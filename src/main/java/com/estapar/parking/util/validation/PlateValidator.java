package com.estapar.parking.util.validation;

import java.util.regex.Pattern;

public class PlateValidator {

    private static final Pattern PLATE_PATTERN = Pattern.compile("^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$");

    public static boolean isValid(String plate) {
        return plate != null && PLATE_PATTERN.matcher(plate).matches();
    }
}
