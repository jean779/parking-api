package com.estapar.parking.config;

import com.estapar.parking.service.GarageService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GarageStartup {

    private final GarageService garageService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        garageService.importGarageData();
    }
}
