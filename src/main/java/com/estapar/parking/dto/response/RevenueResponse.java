package com.estapar.parking.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RevenueResponse {
    private BigDecimal amount;
    private String currency;
    private LocalDateTime timestamp;
}