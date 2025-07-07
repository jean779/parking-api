package com.estapar.parking.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class ParkingSpot {

    @Id
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector", referencedColumnName = "sector")
    private GarageSector sector;
    private double lat;
    private double lng;
    private boolean occupied;
}
