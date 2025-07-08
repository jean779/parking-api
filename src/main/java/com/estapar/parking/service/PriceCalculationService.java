package com.estapar.parking.service;

import com.estapar.parking.config.PriceConfig;
import com.estapar.parking.entity.VehicleEntry;
import com.estapar.parking.repository.ParkingSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class PriceCalculationService {

    private final ParkingSpotRepository parkingSpotRepository;
    private final PriceConfig config;

    public BigDecimal calculatePrice(VehicleEntry entry) {
        long minutesParked = calculateMinutesParked(entry);
        if (minutesParked <= config.getFreeMinutes()) return BigDecimal.ZERO;

        var sector = entry.getSpot().getSector();
        BigDecimal adjustedBasePrice = calculateDynamicBasePrice(sector.getSector(), sector.getBasePrice());

        return minutesParked <= config.getSingleHourLimit()
                ? adjustedBasePrice.setScale(2, RoundingMode.HALF_UP)
                : calculateProratedPrice(adjustedBasePrice, minutesParked);
    }

    private long calculateMinutesParked(VehicleEntry entry) {
        LocalDateTime endTime = entry.getExitTime() != null
                ? entry.getExitTime()
                : LocalDateTime.now();
        return ChronoUnit.MINUTES.between(entry.getEntryTime(), endTime);
    }


    private BigDecimal calculateDynamicBasePrice(String sectorCode, double rawBasePrice) {
        int totalSpots = parkingSpotRepository.countBySectorCode(sectorCode);
        int occupiedSpots = parkingSpotRepository.countBySectorCodeAndOccupiedTrue(sectorCode);

        if (totalSpots == 0) {
            throw new IllegalStateException("Sector has no registered parking spots: " + sectorCode);
        }

        double occupancyRate = (double) occupiedSpots / totalSpots;
        BigDecimal basePrice = BigDecimal.valueOf(rawBasePrice);

        if (occupancyRate < config.getOccupancy().getLow()) return basePrice.multiply(config.getMultiplier().getLow());
        if (occupancyRate < config.getOccupancy().getMedium()) return basePrice.multiply(config.getMultiplier().getMedium());
        if (occupancyRate < config.getOccupancy().getHigh()) return basePrice.multiply(config.getMultiplier().getHigh());
        return basePrice.multiply(config.getMultiplier().getMax());
    }

    private BigDecimal calculateProratedPrice(BigDecimal basePrice, long minutesParked) {
        long extraMinutes = minutesParked - config.getSingleHourLimit();
        long intervals = (long) Math.ceil((double) extraMinutes / config.getIntervalMinutes());

        BigDecimal intervalRate = basePrice.divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP);
        BigDecimal additional = intervalRate.multiply(BigDecimal.valueOf(intervals));

        return basePrice.add(additional).setScale(2, RoundingMode.HALF_UP);
    }
}
