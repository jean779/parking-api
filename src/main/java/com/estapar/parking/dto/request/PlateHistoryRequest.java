package com.estapar.parking.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PlateHistoryRequest {

    private String licensePlate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String sector;
    private int page = 0;
    private int size = 10;
}
