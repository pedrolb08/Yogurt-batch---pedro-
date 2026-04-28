package com.pedro.demo.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class MonitoringDto {
 
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemperatureSummary {
        private double currentTemperature;
        private double maximumTemperature;
        private double averageTemperature;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Dashboard {
        private Map<String, Long> batchCount;
        private long totalBatches;
        private long activeBatches;
        private long completedBatches;
    }
}

