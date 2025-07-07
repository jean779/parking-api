package com.estapar.parking.business;

import com.estapar.parking.dto.request.RevenueHistoryRequest;
import com.estapar.parking.dto.response.RevenueHistoryResponse;
import com.estapar.parking.dto.response.RevenueResponse;
import com.estapar.parking.repository.VehicleEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RevenueBusinessService {

    private final VehicleEntryRepository vehicleEntryRepository;

    public RevenueResponse getRevenue(LocalDate date, String sector) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        BigDecimal totalAmount = vehicleEntryRepository.sumPriceByDateAndSector(start, end, sector);
        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }

        return RevenueResponse.builder()
                .amount(totalAmount.setScale(2, BigDecimal.ROUND_HALF_UP))
                .currency("BRL")
                .timestamp(LocalDateTime.now())
                .build();
    }

    public Page<RevenueHistoryResponse> getRevenueHistory(RevenueHistoryRequest request) {
        LocalDate start = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();
        LocalDate end = request.getEndDate() != null ? request.getEndDate() : start;

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        return vehicleEntryRepository.findRevenueGroupedByDateAndSector(
                request.getSector(),
                start.atStartOfDay(),
                end.plusDays(1).atStartOfDay(),
                pageable
        );
    }

}