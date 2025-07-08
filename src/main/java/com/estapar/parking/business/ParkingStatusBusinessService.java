package com.estapar.parking.business;

import com.estapar.parking.dto.request.PlateHistoryRequest;
import com.estapar.parking.dto.response.PlateHistoryResponse;
import com.estapar.parking.dto.response.PlateStatusResponse;
import com.estapar.parking.dto.response.SpotStatusResponse;
import com.estapar.parking.entity.ParkingSpot;
import com.estapar.parking.entity.VehicleEntry;
import com.estapar.parking.exception.ResourceNotFoundException;
import com.estapar.parking.repository.ParkingSpotRepository;
import com.estapar.parking.repository.VehicleEntryRepository;
import com.estapar.parking.service.PriceCalculationService;
import com.estapar.parking.util.date.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingStatusBusinessService {

    private final VehicleEntryRepository vehicleEntryRepository;
    private final PriceCalculationService priceCalculationService;
    private final ParkingSpotRepository parkingSpotRepository;


    public PlateStatusResponse getPlateStatus(String licensePlate) {
        log.debug("Checking status for plate {}", licensePlate);

        VehicleEntry entry = vehicleEntryRepository
                .findFirstByPlateAndExitTimeIsNullOrderByEntryTimeDesc(licensePlate)
                .orElseThrow(() -> {
                    log.warn("No active entry found for license plate {}", licensePlate);
                    return new ResourceNotFoundException("No active entry found for license plate " + licensePlate);
                });

        BigDecimal price = entry.getSpot() != null
                ? priceCalculationService.calculatePrice(entry)
                : BigDecimal.ZERO;

        LocalDateTime endTime = entry.getExitTime() != null ? entry.getExitTime() : LocalDateTime.now();
        Duration timeParked = Duration.between(entry.getEntryTime(), endTime);

        log.info("Plate {} parked for {} minutes, price so far: {}", licensePlate, timeParked.toMinutes(), price);

        return PlateStatusResponse.builder()
                .licensePlate(licensePlate)
                .priceUntilNow(price)
                .entryTime(entry.getEntryTime())
                .timeParked(TimeUtils.formatDuration(timeParked))
                .build();
    }


    public SpotStatusResponse getSpotStatus(double lat, double lng) {
        log.debug("Checking spot status at lat={}, lng={}", lat, lng);

        ParkingSpot spot = parkingSpotRepository.findByLatAndLng(lat, lng)
                .orElseThrow(() -> {
                    log.warn("No spot found at coordinates {}, {}", lat, lng);
                    return new ResourceNotFoundException("Parking spot not found for coordinates: " + lat + ", " + lng);
                });

        if (spot.isOccupied()) {
            return vehicleEntryRepository.findFirstBySpotAndExitTimeIsNullOrderByEntryTimeDesc(spot)
                    .map(entry -> {
                        Duration duration = Duration.between(entry.getEntryTime(), LocalDateTime.now());
                        log.info("Spot at lat={}, lng={} is occupied for {} minutes", lat, lng, duration.toMinutes());
                        return SpotStatusResponse.builder()
                                .occupied(true)
                                .entryTime(entry.getEntryTime())
                                .timeParked(TimeUtils.formatDuration(duration))
                                .build();
                    }).orElseGet(() -> {
                        log.warn("Spot is marked occupied but no vehicle entry found for it.");
                        return SpotStatusResponse.builder().occupied(true).build();
                    });
        }

        log.info("Spot at lat={}, lng={} is available", lat, lng);
        return SpotStatusResponse.builder().occupied(false).build();
    }

    public Page<PlateHistoryResponse> getPlateHistory(PlateHistoryRequest request) {
        log.debug("Fetching plate history: {}", request.getLicensePlate());

        PageRequest pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<VehicleEntry> page = vehicleEntryRepository
                .findAllByPlateOrderByEntryTimeDesc(request.getLicensePlate(), pageable);

        List<PlateHistoryResponse> responseList = page.getContent().stream().map(entry -> {
            String duration = entry.getExitTime() != null
                    ? TimeUtils.formatDuration(Duration.between(entry.getEntryTime(), entry.getExitTime()))
                    : null;

            return PlateHistoryResponse.builder()
                    .licensePlate(entry.getPlate())
                    .entryTime(entry.getEntryTime())
                    .exitTime(entry.getExitTime())
                    .price(entry.getPrice())
                    .sector(entry.getSpot() != null ? entry.getSpot().getSector().getSector() : null)
                    .timeParked(duration)
                    .build();
        }).collect(Collectors.toList());

        log.info("Retrieved {} records of plate history for {}", responseList.size(), request.getLicensePlate());
        return new PageImpl<>(responseList, pageable, page.getTotalElements());
    }

}
