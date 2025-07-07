package com.estapar.parking.unit;

import com.estapar.parking.business.WebhookBusinessService;
import com.estapar.parking.dto.request.WebhookEventRequest;
import com.estapar.parking.entity.GarageSector;
import com.estapar.parking.entity.ParkingSpot;
import com.estapar.parking.entity.VehicleEntry;
import com.estapar.parking.enums.EventType;
import com.estapar.parking.exception.*;
import com.estapar.parking.repository.VehicleEntryRepository;
import com.estapar.parking.service.ParkingService;
import com.estapar.parking.service.PriceCalculationService;
import com.estapar.parking.util.validation.PlateValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookBusinessServiceTest {

    @InjectMocks
    private WebhookBusinessService service;

    @Mock
    private VehicleEntryRepository vehicleEntryRepository;

    @Mock
    private ParkingService parkingService;

    @Mock
    private PriceCalculationService priceCalculationService;

    @Spy
    @SuppressWarnings("unused")
    private final PlateValidator plateValidator = new PlateValidator();

    private WebhookEventRequest dto;

    @BeforeEach
    void setup() {
        dto = new WebhookEventRequest();
        dto.setLicensePlate("ABC1D23");
    }

    @Test
    @DisplayName("Should process ENTRY event successfully")
    void shouldProcessEntrySuccessfully() {
        dto.setEntryTime("2025-01-01T10:00:00.000Z");
        dto.setEventType(EventType.ENTRY);

        when(vehicleEntryRepository.findFirstByPlateAndExitTimeIsNullOrderByEntryTimeDesc("ABC1D23"))
                .thenReturn(Optional.empty());
        when(vehicleEntryRepository.save(any())).thenReturn(new VehicleEntry());

        assertDoesNotThrow(() -> service.processWebhookEvent(dto));
        verify(vehicleEntryRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should throw if license plate is invalid")
    void shouldThrowIfLicensePlateIsInvalid() {
        dto.setLicensePlate("INVALID");
        dto.setEntryTime("2025-01-01T10:00:00.000Z");
        dto.setEventType(EventType.ENTRY);

        assertThrows(InvalidPlateFormatException.class, () -> service.processWebhookEvent(dto));
        verify(vehicleEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw if vehicle already entered")
    void shouldThrowIfVehicleAlreadyInside() {
        dto.setEntryTime("2025-01-01T10:00:00.000Z");
        dto.setEventType(EventType.ENTRY);

        when(vehicleEntryRepository.findFirstByPlateAndExitTimeIsNullOrderByEntryTimeDesc("ABC1D23"))
                .thenReturn(Optional.of(new VehicleEntry()));

        assertThrows(VehicleAlreadyEnteredException.class, () -> service.processWebhookEvent(dto));
        verify(vehicleEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw if entry time is missing")
    void shouldThrowIfEntryTimeIsMissing() {
        dto.setEventType(EventType.ENTRY);
        dto.setEntryTime(null);

        assertThrows(InvalidRequestException.class, () -> service.processWebhookEvent(dto));
    }

    @Test
    @DisplayName("Should throw if no active entry when parking")
    void shouldThrowIfNoActiveEntryOnParked() {
        dto.setEventType(EventType.PARKED);
        dto.setLat(-23.561684);
        dto.setLng(-46.655981);

        when(vehicleEntryRepository.findFirstByPlateAndExitTimeIsNullOrderByEntryTimeDesc("ABC1D23"))
                .thenReturn(Optional.empty());

        assertThrows(InvalidRequestException.class, () -> service.processWebhookEvent(dto));
        verify(vehicleEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw if spot is already occupied")
    void shouldThrowIfSpotOccupied() {
        dto.setEventType(EventType.PARKED);
        dto.setLat(-23.561684);
        dto.setLng(-46.655981);

        VehicleEntry entry = new VehicleEntry();
        ParkingSpot spot = new ParkingSpot();
        spot.setOccupied(true);

        when(vehicleEntryRepository.findFirstByPlateAndExitTimeIsNullOrderByEntryTimeDesc("ABC1D23"))
                .thenReturn(Optional.of(entry));

        when(parkingService.findSpotByLatLng(dto.getLat(), dto.getLng()))
                .thenReturn(Optional.of(spot));

        assertThrows(SpotOccupiedException.class, () -> service.processWebhookEvent(dto));
        verify(vehicleEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw if sector is closed when parking")
    void shouldThrowIfSectorIsClosed() {
        dto.setEventType(EventType.PARKED);
        dto.setLat(-23.561684);
        dto.setLng(-46.655981);

        VehicleEntry entry = new VehicleEntry();
        ParkingSpot spot = new ParkingSpot();
        spot.setOccupied(false);

        GarageSector sector = new GarageSector();
        sector.setSector("A");
        sector.setOpenHour("08:00");
        sector.setCloseHour("18:00");
        spot.setSector(sector);

        when(vehicleEntryRepository.findFirstByPlateAndExitTimeIsNullOrderByEntryTimeDesc(dto.getLicensePlate()))
                .thenReturn(Optional.of(entry));

        when(parkingService.findSpotByLatLng(dto.getLat(), dto.getLng()))
                .thenReturn(Optional.of(spot));

        when(parkingService.isSectorOpen(eq(sector), any())).thenReturn(false);

        assertThrows(SectorClosedException.class, () -> service.processWebhookEvent(dto));
        verify(vehicleEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw if sector is full when parking")
    void shouldThrowIfSectorIsFull() {
        dto.setEventType(EventType.PARKED);
        dto.setLat(-23.561684);
        dto.setLng(-46.655981);

        VehicleEntry entry = new VehicleEntry();
        ParkingSpot spot = new ParkingSpot();
        spot.setOccupied(false);

        GarageSector sector = new GarageSector();
        sector.setSector("A");
        sector.setOpenHour("08:00");
        sector.setCloseHour("18:00");
        spot.setSector(sector);

        when(vehicleEntryRepository.findFirstByPlateAndExitTimeIsNullOrderByEntryTimeDesc(dto.getLicensePlate()))
                .thenReturn(Optional.of(entry));

        when(parkingService.findSpotByLatLng(dto.getLat(), dto.getLng()))
                .thenReturn(Optional.of(spot));

        when(parkingService.isSectorOpen(eq(sector), any())).thenReturn(true);
        when(parkingService.isSectorFull(sector.getSector())).thenReturn(true);

        assertThrows(SectorFullException.class, () -> service.processWebhookEvent(dto));
        verify(vehicleEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should process EXIT event successfully")
    void shouldProcessExitSuccessfully() {
        dto.setExitTime("2025-01-01T12:00:00.000Z");
        dto.setEventType(EventType.EXIT);

        VehicleEntry entry = new VehicleEntry();
        ParkingSpot spot = new ParkingSpot();
        entry.setSpot(spot);

        when(vehicleEntryRepository.findFirstByPlateAndExitTimeIsNullOrderByEntryTimeDesc("ABC1D23"))
                .thenReturn(Optional.of(entry));

        when(priceCalculationService.calculatePrice(entry)).thenReturn(BigDecimal.TEN);
        when(vehicleEntryRepository.save(any())).thenReturn(entry);

        assertDoesNotThrow(() -> service.processWebhookEvent(dto));
        verify(vehicleEntryRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should throw if exit time is missing")
    void shouldThrowIfExitTimeIsMissing() {
        dto.setExitTime(null);
        dto.setEventType(EventType.EXIT);

        assertThrows(InvalidRequestException.class, () -> service.processWebhookEvent(dto));
    }
}
