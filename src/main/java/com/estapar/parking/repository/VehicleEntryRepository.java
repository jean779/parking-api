package com.estapar.parking.repository;

import com.estapar.parking.dto.response.RevenueHistoryResponse;
import com.estapar.parking.entity.ParkingSpot;
import com.estapar.parking.entity.VehicleEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VehicleEntryRepository extends JpaRepository<VehicleEntry, Long> {
    Optional<VehicleEntry> findFirstBySpotAndExitTimeIsNullOrderByEntryTimeDesc(ParkingSpot spot);

    Optional<VehicleEntry> findFirstByPlateAndExitTimeIsNullOrderByEntryTimeDesc(String plate);

    @Query("SELECT COALESCE(SUM(v.price), 0) FROM VehicleEntry v WHERE v.exitTime BETWEEN :start AND :end AND v.spot.sector.sector = :sector")
    BigDecimal sumPriceByDateAndSector(@Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end,
                                       @Param("sector") String sector);


    Page<VehicleEntry> findAllByPlateOrderByEntryTimeDesc(String plate, Pageable pageable);

    @Query("""
    SELECT NEW com.estapar.parking.dto.response.RevenueHistoryResponse(
        v.exitTime, v.spot.sector.sector, SUM(v.price)
    )
    FROM VehicleEntry v
    WHERE (:sector IS NULL OR v.spot.sector.sector = :sector)
      AND v.exitTime BETWEEN :start AND :end
    GROUP BY  v.spot.sector.sector, v.exitTime
    ORDER BY v.exitTime DESC
    """)
    Page<RevenueHistoryResponse> findRevenueGroupedByDateAndSector(
            @Param("sector") String sector,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );
}