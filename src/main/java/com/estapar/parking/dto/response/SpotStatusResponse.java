package com.estapar.parking.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SpotStatusResponse {
    private boolean occupied;
    private LocalDateTime entryTime;
    private String timeParked;
}