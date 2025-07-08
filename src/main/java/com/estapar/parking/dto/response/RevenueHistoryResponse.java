package com.estapar.parking.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RevenueHistoryResponse {

    private LocalDateTime date;
    private String sector;
    private BigDecimal amount;

    public RevenueHistoryResponse(LocalDateTime dateTime, String sector, BigDecimal amount) {
        this.date = dateTime;
        this.sector = sector;
        this.amount = amount;
    }
}
