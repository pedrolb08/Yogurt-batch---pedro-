package com.pedro.demo.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.pedro.demo.demain.repository.TemperaturaLogRepository;
import com.pedro.demo.demain.repository.YogurtBatchRepository;
import com.pedro.demo.domain.model.TemperaturaLog;
import com.pedro.demo.domain.model.YogurtBatch;
import com.pedro.demo.dto.MonitoringDto;
import com.pedro.demo.servicio.TemperatureControlService;
import com.pedro.demo.servicio.YogurtMakingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
@Tag(name = "Monitoreo y Dashboard", description = "Endpoints para monitoreo en tiempo real de temperaturas, estados de lotes y panel de control general de producción")
public class MonitoringController {

    private final TemperaturaLogRepository temperaturaLogRepository;
    private final YogurtBatchRepository batchRepository;
     private final TemperatureControlService temperatureControlService;

    @GetMapping("/batches/active")
    @Operation(summary = "Obtener lotes activos",
        description = "Retorna todos los lotes que están en proceso activo de producción (PREPARING, HEATING, INNOCULATION, INCUBATING, COOLING, REFRIGERATING)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de lotes activos obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = YogurtBatch.class))),
        @ApiResponse(responseCode = "404", description = "No hay lotes activos en este momento", content = @Content)
    })
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
    @Operation(summary = "Obtener resumen de temperatura de un lote",
        description = "Retorna un resumen estadístico de temperaturas del lote: temperatura actual, máxima registrada y promedio durante el proceso")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Resumen de temperaturas obtenido exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MonitoringDto.TemperatureSummary.class))),
        @ApiResponse(responseCode = "404", description = "Lote no encontrado con el ID proporcionado", content = @Content)
    })
    public ResponseEntity<MonitoringDto.TemperatureSummary> getBatchTemperatureSummary(
            @Parameter(description = "ID único del lote a consultar", required = true, example = "1")
            @PathVariable Long batchId) {
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
    @Operation(summary = "Obtener histórico de temperaturas con filtro de fechas",
        description = "Retorna el registro histórico de temperaturas de un lote. Se puede filtrar por rango de fechas con los parámetros 'start' y 'end'")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Histórico de temperaturas obtenido exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TemperaturaLog.class))),
        @ApiResponse(responseCode = "404", description = "Lote no encontrado con el ID proporcionado", content = @Content),
        @ApiResponse(responseCode = "400", description = "Formato de fecha inválido. Use formato ISO: yyyy-MM-ddTHH:mm:ss", content = @Content)
    })
    public ResponseEntity<List<TemperaturaLog>> getTemperatureLogs(
            @Parameter(description = "ID único del lote", required = true, example = "1")
            @PathVariable Long batchId,
            @Parameter(description = "Fecha y hora de inicio del filtro (ISO format)", example = "2024-01-01T08:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(description = "Fecha y hora de fin del filtro (ISO format)", example = "2024-01-01T20:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        if (start != null && end != null) {
            return ResponseEntity.ok(temperaturaLogRepository.findByBatchAndTimeRange(batchId, start, end));
        }

        return batchRepository.findById(batchId)
                .map(batch -> ResponseEntity.ok(temperaturaLogRepository.findByBatch(batch)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Obtener panel de control general",
        description = "Retorna un resumen ejecutivo con conteos de lotes por estado, total de lotes en el sistema y lotes completados hoy")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Panel de control obtenido exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MonitoringDto.Dashboard.class))),
        @ApiResponse(responseCode = "500", description = "Error interno al calcular las estadísticas del dashboard", content = @Content)
    })
    public ResponseEntity<MonitoringDto.Dashboard> getDashboard() {
        long preparingCount = batchRepository.countByStatus(YogurtBatch.BatchStatus.PREPARING);
        long heatingCount = batchRepository.countByStatus(YogurtBatch.BatchStatus.HEATING);
        long innoculationCount = batchRepository.countByStatus(YogurtBatch.BatchStatus.INNOCULATION);
        long incubatingCount = batchRepository.countByStatus(YogurtBatch.BatchStatus.INCUBATING);
        long coolingCount = batchRepository.countByStatus(YogurtBatch.BatchStatus.COOLING);
        long refrigeratingCount = batchRepository.countByStatus(YogurtBatch.BatchStatus.REFRIGERATING);
        long completedCount = batchRepository.countByStatus(YogurtBatch.BatchStatus.COMPLETED);
        long failedCount = batchRepository.countByStatus(YogurtBatch.BatchStatus.FAILED);

        Map<String, Long> batchCounts = new HashMap<>();
        batchCounts.put("PREPARING", preparingCount);
        batchCounts.put("HEATING", heatingCount);
        batchCounts.put("INNOCULATION", innoculationCount);
        batchCounts.put("INCUBATING", incubatingCount);
        batchCounts.put("COOLING", coolingCount);
        batchCounts.put("REFRIGERATING", refrigeratingCount);
        batchCounts.put("COMPLETED", completedCount);
        batchCounts.put("FAILED", failedCount);

        long activeBatchesCount = preparingCount + heatingCount + innoculationCount + incubatingCount + coolingCount + refrigeratingCount;

        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        List<YogurtBatch> completedToday = batchRepository.findByStatusAndDateRange(
                YogurtBatch.BatchStatus.COMPLETED, startOfDay, endOfDay);

        MonitoringDto.Dashboard dashboard = MonitoringDto.Dashboard.builder()
                .batchCount(batchCounts)
                .totalBatches(batchRepository.count())
                .activeBatches(activeBatchesCount)
                .completedBatches(completedToday.size())
                .build();

        return ResponseEntity.ok(dashboard);
    }
}
