package com.estapar.parking.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PlateStatusResponse {
    private String licensePlate;
    private BigDecimal priceUntilNow;
    private LocalDateTime entryTime;
    private String timeParked;
}
