package com.pedro.demo.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.pedro.demo.demain.repository.TemperaturaLogRepository;
import com.pedro.demo.demain.repository.YogurtBatchRepository;
import com.pedro.demo.domain.model.TemperaturaLog;
import com.pedro.demo.domain.model.YogurtBatch;
import com.pedro.demo.dto.MonitoringDto;
import com.pedro.demo.servicio.YogurtMakingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
@Tag(name = "Monitoreo", description = "Monitoreo de temperatura y estado de los lotes")
public class MonitoringController {

    private final TemperaturaLogRepository temperaturaLogRepository;
    private final YogurtBatchRepository batchRepository;
    private final YogurtMakingService yogurtMakingService;

    @GetMapping("/batches/active")
    @Operation(summary = "Obtener lotes activos")
    public ResponseEntity<List<YogurtBatch>> getActiveBatches() {
        List<YogurtBatch> activeBatches = batchRepository.findByStatus(YogurtBatch.BatchStatus.PREPARING);
        activeBatches.addAll(batchRepository.findByStatus(YogurtBatch.BatchStatus.HEATING));
        activeBatches.addAll(batchRepository.findByStatus(YogurtBatch.BatchStatus.INNOCULATION));
        activeBatches.addAll(batchRepository.findByStatus(YogurtBatch.BatchStatus.INCUBATING));
        activeBatches.addAll(batchRepository.findByStatus(YogurtBatch.BatchStatus.COOLING));
        activeBatches.addAll(batchRepository.findByStatus(YogurtBatch.BatchStatus.REFRIGERATING));
        return ResponseEntity.ok(activeBatches);
    }

    @GetMapping("/batches/{batchId}/temperature")
    @Operation(summary = "Obtener resumen de temperatura de un lote")
    public ResponseEntity<MonitoringDto.TemperatureSummary> getBatchTemperatureSummary(@PathVariable Long batchId) {
        return batchRepository.findById(batchId)
                .map(batch -> {
                    Double currentTemp = batch.getCurrentTemperature();
                    Double maxTemp = temperaturaLogRepository.getMaxTemperatureByBatch(batchId);
                    Double avgTemp = temperaturaLogRepository.getAverageTemperatureByBatchAndType(
                            batchId, TemperaturaLog.LogType.MONITORING);

                    MonitoringDto.TemperatureSummary summary = MonitoringDto.TemperatureSummary.builder()
                            .currentTemperature(currentTemp != null ? currentTemp : 0.0)
                            .maximumTemperature(maxTemp != null ? maxTemp : 0.0)
                            .averageTemperature(avgTemp != null ? avgTemp : 0.0)
                            .build();

                    return ResponseEntity.ok(summary);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/batches/{batchId}/temperature-logs")
    @Operation(summary = "Obtener historico de temperaturas con filtro de fechas")
    public ResponseEntity<List<TemperaturaLog>> getTemperatureLogs(
            @PathVariable Long batchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        if (start != null && end != null) {
            return ResponseEntity.ok(temperaturaLogRepository.findByBatchAndTimeRange(batchId, start, end));
        }

        return batchRepository.findById(batchId)
                .map(batch -> ResponseEntity.ok(temperaturaLogRepository.findByBatch(batch)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Obtener panel de control general")
    public ResponseEntity<MonitoringDto.Dashboard> getDashboard() {
        // Contar lotes por estado
        long preparingCount = batchRepository.countByStatus(YogurtBatch.BatchStatus.PREPARING);
        long heatingCount = batchRepository.countByStatus(YogurtBatch.BatchStatus.HEATING);
        long innoculationCount = batchRepository.countByStatus(YogurtBatch.BatchStatus.INNOCULATION);
        long incubatingCount = batchRepository.countByStatus(YogurtBatch.BatchStatus.INCUBATING);
        long coolingCount = batchRepository.countByStatus(YogurtBatch.BatchStatus.COOLING);
        long refrigeratingCount = batchRepository.countByStatus(YogurtBatch.BatchStatus.REFRIGERATING);
        long completedCount = batchRepository.countByStatus(YogurtBatch.BatchStatus.COMPLETED);
        long failedCount = batchRepository.countByStatus(YogurtBatch.BatchStatus.FAILED);

        // Llenar el mapa de conteos
        Map<String, Long> batchCounts = new HashMap<>();
        batchCounts.put("PREPARING", preparingCount);
        batchCounts.put("HEATING", heatingCount);
        batchCounts.put("INNOCULATION", innoculationCount);
        batchCounts.put("INCUBATING", incubatingCount);
        batchCounts.put("COOLING", coolingCount);
        batchCounts.put("REFRIGERATING", refrigeratingCount);
        batchCounts.put("COMPLETED", completedCount);
        batchCounts.put("FAILED", failedCount);

        // Calcular estadísticas
        long activeBatchesCount = preparingCount + heatingCount + innoculationCount + incubatingCount + coolingCount + refrigeratingCount;
        
        // Lotes completados hoy
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        List<YogurtBatch> completedToday = batchRepository.findByStatusAndDateRange(
                YogurtBatch.BatchStatus.COMPLETED, 
                startOfDay, 
                endOfDay);

        // Construir dashboard
        MonitoringDto.Dashboard dashboard = MonitoringDto.Dashboard.builder()
                .batchCount(batchCounts)
                .totalBatches(batchRepository.count())
                .activeBatches(activeBatchesCount)
                .completedBatches(completedToday.size())
                .build();

        return ResponseEntity.ok(dashboard);
    }
}

