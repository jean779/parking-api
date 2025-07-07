package com.estapar.parking.repository;

import com.estapar.parking.entity.ParkingSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Integer> {
    @Query("SELECT COUNT(p) FROM ParkingSpot p WHERE p.sector.sector = :sectorCode")
    int countBySectorCode(@Param("sectorCode") String sectorCode);

    @Query("SELECT COUNT(p) FROM ParkingSpot p WHERE p.sector.sector = :sectorCode AND p.occupied = true")
    int countBySectorCodeAndOccupiedTrue(@Param("sectorCode") String sectorCode);

    Optional<ParkingSpot> findByLatAndLng(Double lat, Double lng);
}
