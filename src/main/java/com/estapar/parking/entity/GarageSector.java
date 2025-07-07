package com.estapar.parking.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class GarageSector {

    @Id
    private String sector;

    private double basePrice;
    private int maxCapacity;
    private String openHour;
    private String closeHour;
    private int durationLimitMinutes;
}
