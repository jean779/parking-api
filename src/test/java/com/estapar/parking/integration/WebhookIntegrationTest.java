package com.estapar.parking.integration;

import com.estapar.parking.dto.request.WebhookEventRequest;
import com.estapar.parking.enums.EventType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebhookIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnBadRequestWhenLicensePlateIsInvalid() throws Exception {
        WebhookEventRequest request = new WebhookEventRequest();
        request.setLicensePlate("INVALID");
        request.setEventType(EventType.ENTRY);
        request.setEntryTime("2025-01-01T10:00:00");

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnOkWhenEntryEventIsValid() throws Exception {
        WebhookEventRequest request = new WebhookEventRequest();
        request.setLicensePlate("ZZZ1A00");
        request.setEventType(EventType.ENTRY);
        request.setEntryTime("2025-01-01T10:00:00");

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnBadRequestWhenEntryTimeIsMissing() throws Exception {
        WebhookEventRequest request = new WebhookEventRequest();
        request.setLicensePlate("ZZZ1A01");
        request.setEventType(EventType.ENTRY);
        request.setEntryTime(null);

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenExitTimeIsMissing() throws Exception {
        WebhookEventRequest request = new WebhookEventRequest();
        request.setLicensePlate("ZZZ1A02");
        request.setEventType(EventType.EXIT);
        request.setExitTime(null);

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenParkingWithoutEntry() throws Exception {
        WebhookEventRequest request = new WebhookEventRequest();
        request.setLicensePlate("ZZZ1A03");
        request.setEventType(EventType.PARKED);
        request.setLat(-23.561684);
        request.setLng(-46.655981);

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCompleteEntryParkedAndExitSuccessfully() throws Exception {
        String plate = "ZZZ1A04";
        String entryTime = "2025-01-01T10:00:00";
        String exitTime = "2025-01-01T12:00:00";

        WebhookEventRequest entryRequest = new WebhookEventRequest();
        entryRequest.setLicensePlate(plate);
        entryRequest.setEventType(EventType.ENTRY);
        entryRequest.setEntryTime(entryTime);

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entryRequest)))
                .andExpect(status().isOk());

        WebhookEventRequest parkedRequest = new WebhookEventRequest();
        parkedRequest.setLicensePlate(plate);
        parkedRequest.setEventType(EventType.PARKED);
        parkedRequest.setLat(-23.561684);
        parkedRequest.setLng(-46.655981);

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(parkedRequest)))
                .andExpect(status().isOk());

        WebhookEventRequest exitRequest = new WebhookEventRequest();
        exitRequest.setLicensePlate(plate);
        exitRequest.setEventType(EventType.EXIT);
        exitRequest.setExitTime(exitTime);

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exitRequest)))
                .andExpect(status().isOk());
    }
}
