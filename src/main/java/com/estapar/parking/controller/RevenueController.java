package com.estapar.parking.controller;

import com.estapar.parking.business.RevenueBusinessService;
import com.estapar.parking.dto.request.RevenueHistoryRequest;
import com.estapar.parking.dto.response.RevenueHistoryResponse;
import com.estapar.parking.dto.response.RevenueResponse;
import com.estapar.parking.util.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    @GetMapping("/revenue-history")
    public ResponseEntity<ApiResponse<Page<RevenueHistoryResponse>>> getRevenueHistory(
            @ModelAttribute RevenueHistoryRequest request
    ) {
        Page<RevenueHistoryResponse> response = revenueBusinessService.getRevenueHistory(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
