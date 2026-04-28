package com.pedro.demo.dto;

import java.util.List;

import com.pedro.demo.domain.model.Recipe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDto {
    private Long id;
    private String name;
    private String description;
    private Double defaultMilkVolume;
    private Double defaultStarterAmount;
    private Double heatingTemperature;
    private Integer heatingDuration;
    private Double innoculationTemperature;
    private Double incubationTemperature;
    private Integer minIncubationTime;
    private Integer maxIncubationTime;
    private Integer refrigerationTime;
    private Recipe.DifficultyLevel difficulty;
    private String tips;
    private Boolean active;
    private List<IngredientDto> ingredients;
}

