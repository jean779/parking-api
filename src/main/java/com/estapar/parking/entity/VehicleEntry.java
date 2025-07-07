package com.estapar.parking.entity;

import com.estapar.parking.enums.VehicleStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
public class VehicleEntry {

    @Id
    @GeneratedValue
    private Long id;
    private String plate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id")
    private ParkingSpot spot;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatus status;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private BigDecimal price;
    private Double lat;
    private Double lng;
}
