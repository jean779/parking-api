package com.estapar.parking.service;

import com.estapar.parking.entity.VehicleEntry;
import com.estapar.parking.repository.ParkingSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class PriceCalculationService {
    private final ParkingSpotRepository parkingSpotRepository;

    public BigDecimal calculatePrice(VehicleEntry entry) {
        long minutesParked = calculateMinutesParked(entry);
        if (minutesParked <= 15) return BigDecimal.ZERO;

        var sector = entry.getSpot().getSector();
        BigDecimal adjustedBasePrice = calculateDynamicBasePrice(sector.getSector(), sector.getBasePrice());

        return minutesParked <= 60
                ? adjustedBasePrice.setScale(2, RoundingMode.HALF_UP)
                : calculateProratedPrice(adjustedBasePrice, minutesParked);
    }

    private long calculateMinutesParked(VehicleEntry entry) {
        return ChronoUnit.MINUTES.between(entry.getEntryTime(), entry.getExitTime());
    }

    private BigDecimal calculateDynamicBasePrice(String sectorCode, double rawBasePrice) {
        int totalSpots = parkingSpotRepository.countBySectorCode(sectorCode);
        int occupiedSpots = parkingSpotRepository.countBySectorCodeAndOccupiedTrue(sectorCode);

        if (totalSpots == 0) {
            throw new IllegalStateException("Sector has no registered parking spots: " + sectorCode);
        }

        double occupancyRate = (double) occupiedSpots / totalSpots;
        BigDecimal basePrice = BigDecimal.valueOf(rawBasePrice);

        if (occupancyRate < 0.25) return basePrice.multiply(BigDecimal.valueOf(0.9));
        if (occupancyRate < 0.5) return basePrice;
        if (occupancyRate < 0.75) return basePrice.multiply(BigDecimal.valueOf(1.1));
        return basePrice.multiply(BigDecimal.valueOf(1.25));
    }

    private BigDecimal calculateProratedPrice(BigDecimal basePrice, long minutesParked) {
        long extraMinutes = minutesParked - 60;
        long intervals = (long) Math.ceil(extraMinutes / 15.0);

        BigDecimal intervalRate = basePrice.divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP);
        BigDecimal additional = intervalRate.multiply(BigDecimal.valueOf(intervals));

        return basePrice.add(additional).setScale(2, RoundingMode.HALF_UP);
    }
}
