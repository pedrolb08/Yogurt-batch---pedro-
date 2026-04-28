package com.pedro.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class BatchDto {
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StartBatchRequest {
        private Long recipeId;
        private Double customMilkVolume;
        private Double customStarterAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailRequest {
        private String reason;
    }
}

