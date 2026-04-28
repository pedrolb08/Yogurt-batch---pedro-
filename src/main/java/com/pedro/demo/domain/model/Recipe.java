package com.pedro.demo.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "recipes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Double defaultMilkVolume;

    @Column(nullable = false)
    private Double defaultStarterAmount;

    @Column(nullable = false)
    private Double heatingTemperature;

    @Column(nullable = false)
    private Integer heatingDuration;

    @Column(nullable = false)
    private Double innoculationTemperature;

    @Column(nullable = false)
    private Double incubationTemperature;

    @Column(nullable = false)
    private Integer minIncubationTime;

    @Column(nullable = false)
    private Integer maxIncubationTime;

    @Column(nullable = false)
    private Integer refrigerationTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DifficultyLevel difficulty;

    @Column(length = 1000)
    private String tips;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Ingredient> ingredients;

    public enum DifficultyLevel {
        EASY, MEDIUM, DIFFICULT, EXPERT
    }
}

