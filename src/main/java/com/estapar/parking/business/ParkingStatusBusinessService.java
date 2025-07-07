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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParkingStatusBusinessService {

    private final VehicleEntryRepository vehicleEntryRepository;
    private final PriceCalculationService priceCalculationService;
    private final ParkingSpotRepository parkingSpotRepository;

    public PlateStatusResponse getPlateStatus(String licensePlate) {
        VehicleEntry entry = vehicleEntryRepository
                .findFirstByPlateAndExitTimeIsNullOrderByEntryTimeDesc(licensePlate)
                .orElseThrow(() -> new ResourceNotFoundException("No active entry found for license plate " + licensePlate));

        BigDecimal price = priceCalculationService.calculatePrice(entry);
        Duration timeParked = Duration.between(entry.getEntryTime(), LocalDateTime.now());

        return PlateStatusResponse.builder()
                .licensePlate(licensePlate)
                .priceUntilNow(price)
                .entryTime(entry.getEntryTime())
                .timeParked(TimeUtils.formatDuration(timeParked))
                .build();
    }


    public SpotStatusResponse getSpotStatus(double lat, double lng) {
        ParkingSpot spot = parkingSpotRepository.findByLatAndLng(lat, lng)
                .orElseThrow(() -> new ResourceNotFoundException("Parking spot not found for coordinates: " + lat + ", " + lng));

        Optional<VehicleEntry> optionalEntry = vehicleEntryRepository.findFirstBySpotAndExitTimeIsNullOrderByEntryTimeDesc(spot);

        if (optionalEntry.isPresent()) {
            VehicleEntry entry = optionalEntry.get();
            LocalDateTime now = LocalDateTime.now();

            return SpotStatusResponse.builder()
                    .occupied(true)
                    .entryTime(entry.getEntryTime())
                    .timeParked(TimeUtils.formatDuration(Duration.between(entry.getEntryTime(), now)))
                    .build();
        } else {
            return SpotStatusResponse.builder()
                    .occupied(false)
                    .build();
        }
    }

    public Page<PlateHistoryResponse> getPlateHistory(PlateHistoryRequest request) {
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

        return new PageImpl<>(responseList, pageable, page.getTotalElements());
    }

}
