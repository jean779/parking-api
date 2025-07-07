package com.estapar.parking.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RevenueHistoryResponse {

    private LocalDate date;
    private String sector;
    private BigDecimal amount;

    public RevenueHistoryResponse(LocalDateTime dateTime, String sector, BigDecimal amount) {
        this.date = dateTime.toLocalDate();
        this.sector = sector;
        this.amount = amount;
    }
}
