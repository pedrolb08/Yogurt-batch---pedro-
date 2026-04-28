package com.pedro.demo.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "yogurt_batches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YogurtBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String batchCode;

    @ManyToOne
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Column(nullable = false)
    private Double milkVolume;

    @Column(nullable = false)
    private Double starterAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BatchStatus status = BatchStatus.PREPARING;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime completedAt;

    @Column(length = 500)
    private String notes;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TemperaturaLog> temperatureLogs;

    @Column(nullable = false)
    @Builder.Default
    private Double currentTemperature = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Double targetTemperature = 0.0;

    public enum BatchStatus {
        PREPARING, HEATING, INNOCULATION, INCUBATING, COOLING, REFRIGERATING, COMPLETED, FAILED
    }
}
