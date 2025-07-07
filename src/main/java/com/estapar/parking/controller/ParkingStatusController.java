package com.estapar.parking.controller;

import com.estapar.parking.dto.request.PlateHistoryRequest;
import com.estapar.parking.dto.response.PlateHistoryResponse;
import com.estapar.parking.dto.response.PlateStatusResponse;
import com.estapar.parking.dto.response.SpotStatusResponse;
import com.estapar.parking.business.ParkingStatusBusinessService;
import com.estapar.parking.util.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/parking-status")
@RequiredArgsConstructor
public class ParkingStatusController {

    private final ParkingStatusBusinessService parkingStatusBusinessService;

    @GetMapping("/plate")
    public ResponseEntity<ApiResponse<PlateStatusResponse>> getPlateStatus(@RequestParam("license_plate") String plate) {
        PlateStatusResponse response = parkingStatusBusinessService.getPlateStatus(plate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/spot")
    public ResponseEntity<ApiResponse<SpotStatusResponse>> getSpotStatus(@RequestParam double lat, @RequestParam double lng) {
        SpotStatusResponse response = parkingStatusBusinessService.getSpotStatus(lat, lng);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/plate-history")
    public ResponseEntity<ApiResponse<Page<PlateHistoryResponse>>> getPlateHistory(
            @RequestParam String licensePlate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PlateHistoryRequest request = new PlateHistoryRequest();
        request.setLicensePlate(licensePlate);
        request.setPage(page);
        request.setSize(size);

        Page<PlateHistoryResponse> response = parkingStatusBusinessService.getPlateHistory(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
