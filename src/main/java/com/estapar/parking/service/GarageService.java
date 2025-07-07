package com.estapar.parking.service;

import com.estapar.parking.dto.GarageConfigDTO;
import com.estapar.parking.entity.GarageSector;
import com.estapar.parking.entity.ParkingSpot;
import com.estapar.parking.repository.GarageSectorRepository;
import com.estapar.parking.repository.ParkingSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GarageService {

    private final GarageSectorRepository garageSectorRepository;
    private final ParkingSpotRepository parkingSpotRepository;
    private final RestTemplate restTemplate;

    @Value("${garage.api.url}")
    private String garageUrl;

    public void importGarageData() {
        GarageConfigDTO dto = restTemplate.getForObject(garageUrl, GarageConfigDTO.class);

        if (dto != null) {
            List<GarageSector> sectors = dto.getGarage().stream().map(s -> {
                GarageSector sector = new GarageSector();
                sector.setSector(s.getSector());
                sector.setBasePrice(s.getBasePrice());
                sector.setMaxCapacity(s.getMaxCapacity());
                sector.setOpenHour(s.getOpenHour());
                sector.setCloseHour(s.getCloseHour());
                sector.setDurationLimitMinutes(s.getDurationLimitMinutes());
                return sector;
            }).toList();

            garageSectorRepository.saveAll(sectors);

            Map<String, GarageSector> sectorMap = garageSectorRepository.findAll().stream()
                    .collect(Collectors.toMap(GarageSector::getSector, s -> s));

            List<ParkingSpot> spots = dto.getSpots().stream()
                    .map(s -> {
                        ParkingSpot spot = new ParkingSpot();
                        spot.setId(s.getId());
                        spot.setLat(s.getLat());
                        spot.setLng(s.getLng());
                        spot.setOccupied(false);
                        spot.setSector(sectorMap.get(s.getSector()));
                        return spot;
                    }).toList();

            parkingSpotRepository.saveAll(spots);
        }
    }
}
