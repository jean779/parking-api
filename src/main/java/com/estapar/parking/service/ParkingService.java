package com.estapar.parking.service;

import com.estapar.parking.entity.GarageSector;
import com.estapar.parking.entity.ParkingSpot;
import com.estapar.parking.repository.ParkingSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParkingService {

    private final ParkingSpotRepository parkingSpotRepository;

    public Optional<ParkingSpot> findSpotByLatLng(double lat, double lng) {
        return parkingSpotRepository.findByLatAndLng(lat, lng);
    }

    public void saveSpot(ParkingSpot spot) {
        parkingSpotRepository.save(spot);
    }

    public boolean isSectorFull(String sectorCode) {
        validateSectorCode(sectorCode);
        int totalSpots = parkingSpotRepository.countBySectorCode(sectorCode);
        if (totalSpots == 0) {
            throw new IllegalStateException("No parking spots registered for sector: " + sectorCode);
        }
        int occupiedSpots = parkingSpotRepository.countBySectorCodeAndOccupiedTrue(sectorCode);
        return occupiedSpots >= totalSpots;
    }

    public boolean isSectorOpen(GarageSector sector, LocalTime currentTime) {
        LocalTime open = LocalTime.parse(sector.getOpenHour());
        LocalTime close = LocalTime.parse(sector.getCloseHour());
        return !currentTime.isBefore(open) && !currentTime.isAfter(close);
    }


    private void validateSectorCode(String sectorCode) {
        if (sectorCode == null || sectorCode.isBlank()) {
            throw new IllegalArgumentException("Sector code must not be null or blank.");
        }
    }
}
