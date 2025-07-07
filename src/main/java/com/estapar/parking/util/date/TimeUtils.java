package com.estapar.parking.util.date;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public final class TimeUtils {

    private TimeUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private static final List<DateTimeFormatter> SUPPORTED_FORMATTERS = List.of(
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
    );

    public static String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static LocalDateTime parseDateTime(String dateTimeStr) {
        for (DateTimeFormatter formatter : SUPPORTED_FORMATTERS) {
            Optional<LocalDateTime> parsed = tryParseLocal(dateTimeStr, formatter);
            if (parsed.isPresent()) return parsed.get();

            parsed = tryParseOffset(dateTimeStr, formatter);
            if (parsed.isPresent()) return parsed.get();
        }

        throw new DateTimeParseException("Unsupported date format: " + dateTimeStr, dateTimeStr, 0);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return DEFAULT_FORMATTER.format(dateTime);
    }

    private static Optional<LocalDateTime> tryParseLocal(String input, DateTimeFormatter formatter) {
        try {
            return Optional.of(LocalDateTime.parse(input, formatter));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    private static Optional<LocalDateTime> tryParseOffset(String input, DateTimeFormatter formatter) {
        try {
            return Optional.of(OffsetDateTime.parse(input, formatter).toLocalDateTime());
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }
}
