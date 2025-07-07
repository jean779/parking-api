package com.estapar.parking.controller;

import com.estapar.parking.business.RevenueBusinessService;
import com.estapar.parking.util.api.ApiResponse;
import com.estapar.parking.dto.response.RevenueResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/revenue")
@RequiredArgsConstructor
public class RevenueController {

    private final RevenueBusinessService revenueBusinessService;

    @GetMapping
    public ResponseEntity<ApiResponse<RevenueResponse>> getRevenue(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("sector") String sector) {

        RevenueResponse response = revenueBusinessService.getRevenue(date, sector);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
