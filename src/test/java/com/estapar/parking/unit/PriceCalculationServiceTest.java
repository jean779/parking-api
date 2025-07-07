package com.estapar.parking.unit;

import com.estapar.parking.config.PriceConfig;
import com.estapar.parking.entity.GarageSector;
import com.estapar.parking.entity.ParkingSpot;
import com.estapar.parking.entity.VehicleEntry;
import com.estapar.parking.repository.ParkingSpotRepository;
import com.estapar.parking.service.PriceCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PriceCalculationServiceTest {

    @InjectMocks
    private PriceCalculationService priceCalculationService;

    @Mock
    private ParkingSpotRepository parkingSpotRepository;

    @Mock
    private PriceConfig priceConfig;

    private static final String SECTOR_CODE = "A";
    private static final double BASE_PRICE = 10.0;

    private PriceConfig.Occupancy occupancy;
    private PriceConfig.Multiplier multiplier;

    @BeforeEach
    void setup() {
        occupancy = new PriceConfig.Occupancy();
        occupancy.setLow(0.25);
        occupancy.setMedium(0.5);
        occupancy.setHigh(0.75);

        multiplier = new PriceConfig.Multiplier();
        multiplier.setLow(BigDecimal.valueOf(0.9));
        multiplier.setMedium(BigDecimal.valueOf(1.0));
        multiplier.setHigh(BigDecimal.valueOf(1.1));
        multiplier.setMax(BigDecimal.valueOf(1.25));
    }

    @Test
    @DisplayName("Should return ZERO when parked for 15 minutes or less")
    void shouldReturnZeroWhenParkedUpTo15Minutes() {
        when(priceConfig.getFreeMinutes()).thenReturn(15);
        VehicleEntry entry = createEntry(10);
        BigDecimal result = priceCalculationService.calculatePrice(entry);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    @DisplayName("Should apply 10% discount when occupancy is below 25%")
    void shouldApply10PercentDiscountWhenOccupancyIsBelow25() {
        mockConfigs();
        VehicleEntry entry = createEntry(70);
        mockOccupancy(SECTOR_CODE, 100, 20);
        BigDecimal expected = calculateExpected(BASE_PRICE * 0.9, 70);
        BigDecimal result = priceCalculationService.calculatePrice(entry);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Should keep base price when occupancy is below 50%")
    void shouldKeepBasePriceWhenOccupancyIsBelow50() {
        mockConfigs();
        VehicleEntry entry = createEntry(70);
        mockOccupancy(SECTOR_CODE, 100, 40);
        BigDecimal expected = calculateExpected(BASE_PRICE, 70);
        BigDecimal result = priceCalculationService.calculatePrice(entry);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Should increase price by 10% when occupancy is below 75%")
    void shouldIncreasePriceBy10PercentWhenOccupancyIsBelow75() {
        mockConfigs();
        VehicleEntry entry = createEntry(70);
        mockOccupancy(SECTOR_CODE, 100, 70);
        BigDecimal expected = calculateExpected(BASE_PRICE * 1.1, 70);
        BigDecimal result = priceCalculationService.calculatePrice(entry);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Should increase price by 25% when occupancy is 75% or more")
    void shouldIncreasePriceBy25PercentWhenOccupancyIs75OrMore() {
        mockConfigs();
        VehicleEntry entry = createEntry(90);
        mockOccupancy(SECTOR_CODE, 100, 90);
        BigDecimal expected = calculateExpected(BASE_PRICE * 1.25, 90);
        BigDecimal result = priceCalculationService.calculatePrice(entry);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Should throw exception when sector has zero registered spots")
    void shouldThrowWhenSectorHasNoSpots() {
        VehicleEntry entry = createEntry(60);
        when(parkingSpotRepository.countBySectorCode(SECTOR_CODE)).thenReturn(0);
        when(priceConfig.getFreeMinutes()).thenReturn(15);
        assertThrows(IllegalStateException.class, () -> priceCalculationService.calculatePrice(entry));
    }

    private void mockConfigs() {
        when(priceConfig.getFreeMinutes()).thenReturn(15);
        when(priceConfig.getIntervalMinutes()).thenReturn(15);
        when(priceConfig.getSingleHourLimit()).thenReturn(60);
        when(priceConfig.getOccupancy()).thenReturn(occupancy);
        when(priceConfig.getMultiplier()).thenReturn(multiplier);
    }

    private VehicleEntry createEntry(long minutesParked) {
        LocalDateTime now = LocalDateTime.now();
        VehicleEntry entry = new VehicleEntry();
        entry.setEntryTime(now.minusMinutes(minutesParked));
        entry.setExitTime(now);
        entry.setSpot(mockSpot(SECTOR_CODE, BASE_PRICE));
        return entry;
    }

    private ParkingSpot mockSpot(String sectorCode, double basePrice) {
        GarageSector sector = new GarageSector();
        sector.setSector(sectorCode);
        sector.setBasePrice(basePrice);
        ParkingSpot spot = new ParkingSpot();
        spot.setSector(sector);
        return spot;
    }

    private void mockOccupancy(String sectorCode, int total, int occupied) {
        when(parkingSpotRepository.countBySectorCode(sectorCode)).thenReturn(total);
        when(parkingSpotRepository.countBySectorCodeAndOccupiedTrue(sectorCode)).thenReturn(occupied);
    }

    private BigDecimal calculateExpected(double adjustedBase, long totalMinutes) {
        long free = 15;
        long limit = 60;
        long interval = 15;

        if (totalMinutes <= free) return BigDecimal.ZERO;
        BigDecimal base = BigDecimal.valueOf(adjustedBase);

        if (totalMinutes <= limit) return base.setScale(2, RoundingMode.HALF_UP);

        long extra = totalMinutes - limit;
        long intervals = (long) Math.ceil((double) extra / interval);
        BigDecimal intervalValue = base.divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP);
        return base.add(intervalValue.multiply(BigDecimal.valueOf(intervals))).setScale(2, RoundingMode.HALF_UP);
    }
}
