package com.estapar.parking.business;

import com.estapar.parking.dto.request.RevenueHistoryRequest;
import com.estapar.parking.dto.response.RevenueHistoryResponse;
import com.estapar.parking.dto.response.RevenueResponse;
import com.estapar.parking.repository.VehicleEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RevenueBusinessService {

    private final VehicleEntryRepository vehicleEntryRepository;

    public RevenueResponse getRevenue(LocalDate date, String sector) {
        log.debug("Calculating revenue for sector {} on {}", sector, date);

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        BigDecimal totalAmount = vehicleEntryRepository.sumPriceByDateAndSector(start, end, sector);
        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }

        log.info("Revenue for sector {} on {}: {}", sector, date, totalAmount);

        return RevenueResponse.builder()
                .amount(totalAmount.setScale(2, BigDecimal.ROUND_HALF_UP))
                .currency("BRL")
                .timestamp(LocalDateTime.now())
                .build();
    }

    public Page<RevenueHistoryResponse> getRevenueHistory(RevenueHistoryRequest request) {
        log.debug("Fetching revenue history: sector={}, start={}, end={}",
                request.getSector(), request.getStartDate(), request.getEndDate());

        LocalDate start = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();
        LocalDate end = request.getEndDate() != null ? request.getEndDate() : start;

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        Page<RevenueHistoryResponse> result = vehicleEntryRepository.findRevenueGroupedByDateAndSector(
                request.getSector(),
                start.atStartOfDay(),
                end.plusDays(1).atStartOfDay(),
                pageable
        );

        log.info("Retrieved {} revenue records for sector {}", result.getContent().size(), request.getSector());
        return result;
    }

}