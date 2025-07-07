package com.estapar.parking.dto;

import lombok.Data;

import java.util.List;

@Data
public class GarageConfigDTO {
    private List<SectorDTO> garage;
    private List<SpotDTO> spots;

    @Data
    public static class SectorDTO {
        private String sector;
        private double basePrice;
        private int maxCapacity;
        private String openHour;
        private String closeHour;
        private int durationLimitMinutes;
    }

    @Data
    public static class SpotDTO {
        private int id;
        private String sector;
        private double lat;
        private double lng;
        private boolean occupied;
    }
}
