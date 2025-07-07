package com.estapar.parking.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Data
@Configuration
@ConfigurationProperties(prefix = "pricing")
public class PriceConfig {
    private int freeMinutes;
    private int singleHourLimit;
    private int intervalMinutes;

    private Occupancy occupancy = new Occupancy();
    private Multiplier multiplier = new Multiplier();

    @Data
    public static class Occupancy {
        private double low;
        private double medium;
        private double high;
    }

    @Data
    public static class Multiplier {
        private BigDecimal low;
        private BigDecimal medium;
        private BigDecimal high;
        private BigDecimal max;
    }
}
