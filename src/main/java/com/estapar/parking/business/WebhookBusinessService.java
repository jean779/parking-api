package com.estapar.parking.business;

import com.estapar.parking.dto.request.WebhookEventRequest;
import com.estapar.parking.entity.VehicleEntry;
import com.estapar.parking.enums.VehicleStatus;
import com.estapar.parking.exception.*;
import com.estapar.parking.repository.VehicleEntryRepository;
import com.estapar.parking.service.ParkingService;
import com.estapar.parking.service.PriceCalculationService;
import com.estapar.parking.util.date.DateValidator;
import com.estapar.parking.util.validation.PlateValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookBusinessService {

    private final VehicleEntryRepository vehicleEntryRepository;
    private final ParkingService parkingService;
    private final PriceCalculationService priceCalculationService;

    public void processWebhookEvent(WebhookEventRequest dto) {
        log.debug("Received webhook event: plate={}, eventType={}", dto.getLicensePlate(), dto.getEventType());

        if (!PlateValidator.isValid(dto.getLicensePlate())) {
            log.warn("Invalid license plate format: {}", dto.getLicensePlate());
            throw new InvalidPlateFormatException("Invalid license plate format. Example: ABC1D23");
        }

        switch (dto.getEventType()) {
            case ENTRY -> handleEntry(dto);
            case PARKED -> handleParked(dto);
            case EXIT -> handleExit(dto);
            default -> {
                log.warn("Unsupported event type received: {}", dto.getEventType());
                throw new InvalidRequestException("Unsupported event type: " + dto.getEventType());
            }
        }
    }

    private void handleEntry(WebhookEventRequest dto) {
        if (dto.getEntryTime() == null || dto.getEntryTime().isBlank()) {
            log.warn("Missing entryTime for ENTRY event on plate {}", dto.getLicensePlate());
            throw new InvalidRequestException("Field entryTime is required for ENTRY event.");
        }
        LocalDateTime entryTime = DateValidator.parseOrThrow(dto.getEntryTime());

        boolean vehicleInside = vehicleEntryRepository
                .findFirstByPlateAndExitTimeIsNullOrderByEntryTimeDesc(dto.getLicensePlate())
                .isPresent();

        if (vehicleInside) {
            log.warn("Attempt to enter vehicle already inside: {}", dto.getLicensePlate());
            throw new VehicleAlreadyEnteredException("Vehicle with plate " + dto.getLicensePlate() + " is already inside.");
        }

        VehicleEntry entry = new VehicleEntry();
        entry.setPlate(dto.getLicensePlate());
        entry.setEntryTime(entryTime);
        entry.setStatus(VehicleStatus.CHECKED_IN);
        vehicleEntryRepository.save(entry);

        log.info("Vehicle entry recorded for plate {}", dto.getLicensePlate());
    }

    private void handleParked(WebhookEventRequest dto) {
        VehicleEntry entry = vehicleEntryRepository
                .findFirstByPlateAndExitTimeIsNullOrderByEntryTimeDesc(dto.getLicensePlate())
                .orElseThrow(() -> {
                    log.warn("No active entry found for plate {}", dto.getLicensePlate());
                    return new InvalidRequestException("No active entry found for plate " + dto.getLicensePlate());
                });

        entry.setLat(dto.getLat());
        entry.setLng(dto.getLng());

        LocalDateTime now = LocalDateTime.now();

        parkingService.findSpotByLatLng(dto.getLat(), dto.getLng())
                .ifPresentOrElse(spot -> {
                    if (spot.isOccupied()) {
                        log.warn("Spot already occupied at lat={}, lng={}", dto.getLat(), dto.getLng());
                        throw new SpotOccupiedException("Spot is already occupied.");
                    }
                    String sectorCode = spot.getSector().getSector();

                    if (!parkingService.isSectorOpen(spot.getSector(), now.toLocalTime())) {
                        log.warn("Attempt to park outside sector operating hours: sector={}, time={}", sectorCode, now);
                        throw new SectorClosedException("Sector " + sectorCode + " is closed at this time.");
                    }
                    if (parkingService.isSectorFull(sectorCode)) {
                        log.warn("Sector {} is full, cannot assign spot", sectorCode);
                        throw new SectorFullException("Sector is full, cannot assign spot.");
                    }
                    entry.setSpot(spot);
                    spot.setOccupied(true);
                    parkingService.saveSpot(spot);
                    log.info("Vehicle with plate {} parked at spot id {}", dto.getLicensePlate(), spot.getId());
                }, () -> {
                    log.warn("Spot not found for coordinates lat={}, lng={}", dto.getLat(), dto.getLng());
                    throw new InvalidRequestException("Spot not found for the provided coordinates.");
                });
        entry.setStatus(VehicleStatus.PARKED);
        vehicleEntryRepository.save(entry);
    }

    private void handleExit(WebhookEventRequest dto) {
        if (dto.getExitTime() == null || dto.getExitTime().isBlank()) {
            log.warn("Missing exitTime for EXIT event on plate {}", dto.getLicensePlate());
            throw new InvalidRequestException("Field exitTime is required for EXIT event.");
        }
        LocalDateTime exitTime = DateValidator.parseOrThrow(dto.getExitTime());

        VehicleEntry entry = vehicleEntryRepository
                .findFirstByPlateAndExitTimeIsNullOrderByEntryTimeDesc(dto.getLicensePlate())
                .orElseThrow(() -> {
                    log.warn("No active entry found for plate {}", dto.getLicensePlate());
                    return new InvalidRequestException("No active entry found for plate " + dto.getLicensePlate());
                });

        entry.setExitTime(exitTime);
        entry.setStatus(VehicleStatus.EXITED);

        if (entry.getSpot() != null) {
            entry.setPrice(priceCalculationService.calculatePrice(entry));
            entry.getSpot().setOccupied(false);
            parkingService.saveSpot(entry.getSpot());
            log.info("Vehicle {} exited from occupied spot id {} with calculated price.", dto.getLicensePlate(), entry.getSpot().getId());
        } else {
            entry.setPrice(BigDecimal.ZERO);
            log.info("Vehicle {} exited without occupying a spot. No charge applied.", dto.getLicensePlate());
        }

        vehicleEntryRepository.save(entry);
        log.info("Vehicle exit recorded for plate {}", dto.getLicensePlate());
    }

}
