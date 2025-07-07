package com.estapar.parking.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PlateHistoryResponse {
    private String licensePlate;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private BigDecimal price;
    private String sector;
    private String timeParked;
}
