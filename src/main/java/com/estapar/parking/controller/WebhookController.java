package com.estapar.parking.controller;

import com.estapar.parking.dto.request.WebhookEventRequest;
import com.estapar.parking.business.WebhookBusinessService;
import com.estapar.parking.util.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookBusinessService webhookBusinessService;

    @PostMapping
    public ResponseEntity<?> receiveWebhook(@RequestBody WebhookEventRequest dto) {
        webhookBusinessService.processWebhookEvent(dto);
        return ResponseEntity.ok(ApiResponse.success(null, "Event processed successfully."));
    }
}
