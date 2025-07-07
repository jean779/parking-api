package com.estapar.parking.dto.request;

import com.estapar.parking.enums.EventType;
import lombok.Data;

@Data
public class WebhookEventRequest {
    private String licensePlate;
    private EventType eventType;
    private String entryTime;
    private String exitTime;
    private Double lat;
    private Double lng;
}

