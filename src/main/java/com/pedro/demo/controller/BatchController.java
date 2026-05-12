package com.pedro.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.pedro.demo.demain.repository.YogurtBatchRepository;
import com.pedro.demo.domain.model.YogurtBatch;
import com.pedro.demo.dto.BatchDto;
import com.pedro.demo.servicio.YogurtMakingService;
import com.pedro.demo.servicio.TemperatureControlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequestMapping("/api/batches")
@RequiredArgsConstructor
@Tag(name = "Gestión de Lotes", description = "Operaciones para iniciar, consultar y gestionar los lotes de producción de yogur")
public class BatchController {

    private final YogurtBatchRepository batchRepository;
    private final YogurtMakingService yogurtMakingService;
    private final TemperatureControl Service temperatureControlService;

    @GetMapping
    @Operation(summary = "Obtener todos los lotes",
        description = "Retorna la lista completa de todos los lotes de producción registrados en el sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de lotes obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = YogurtBatch.class))),
        @ApiResponse(responseCode = "404", description = "No se encontraron lotes registrados", content = @Content)
    })
    public ResponseEntity<List<YogurtBatch>> getAllBatches() {
        // Obtener todos los lotes de todos los estados
        List<YogurtBatch> allBatches = yogurtMakingService.getBatchesByStatus(YogurtBatch.BatchStatus.PREPARING);
        allBatches.addAll(yogurtMakingService.getBatchesByStatus(YogurtBatch.BatchStatus.HEATING));
        allBatches.addAll(yogurtMakingService.getBatchesByStatus(YogurtBatch.BatchStatus.INNOCULATION));
        allBatches.addAll(yogurtMakingService.getBatchesByStatus(YogurtBatch.BatchStatus.INCUBATING));
        allBatches.addAll(yogurtMakingService.getBatchesByStatus(YogurtBatch.BatchStatus.COOLING));
        allBatches.addAll(yogurtMakingService.getBatchesByStatus(YogurtBatch.BatchStatus.REFRIGERATING));
        allBatches.addAll(yogurtMakingService.getBatchesByStatus(YogurtBatch.BatchStatus.COMPLETED));
        allBatches.addAll(yogurtMakingService.getBatchesByStatus(YogurtBatch.BatchStatus.FAILED));
        return ResponseEntity.ok(allBatches);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener lote por ID",
        description = "Busca y retorna un lote de producción específico usando su identificador único")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lote encontrado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = YogurtBatch.class))),
        @ApiResponse(responseCode = "404", description = "Lote no encontrado con el ID proporcionado", content = @Content)
    })
    public ResponseEntity<YogurtBatch> getBatchById(
            @Parameter(description = "ID único del lote", required = true, example = "1")
            @PathVariable Long id) {
        return batchRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/start")
    @Operation(summary = "Iniciar nuevo lote de yogur",
        description = "Crea e inicia un nuevo lote de producción basado en una receta existente. Permite personalizar volumen de leche y cantidad de cultivo")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Lote iniciado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = YogurtBatch.class))),
        @ApiResponse(responseCode = "404", description = "Receta no encontrada con el ID proporcionado", content = @Content),
        @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud", content = @Content)
    })
    public ResponseEntity<YogurtBatch> startBatch(@RequestBody BatchDto.StartBatchRequest request) {
        YogurtBatch batch = yogurtMakingService.startBatch(
                request.getRecipeId(),
                request.getCustomMilkVolume(),
                request.getCustomStarterAmount()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(batch);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Obtener lotes por estado",
        description = "Filtra y retorna los lotes que se encuentran en un estado específico del proceso de producción")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de lotes filtrados por estado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = YogurtBatch.class))),
        @ApiResponse(responseCode = "400", description = "Estado inválido. Valores permitidos: PREPARING, HEATING, INNOCULATION, INCUBATING, COOLING, REFRIGERATING, COMPLETED, FAILED", content = @Content)
    })
    public ResponseEntity<List<YogurtBatch>> getBatchesByStatus(
            @Parameter(description = "Estado del lote", required = true, example = "INCUBATING")
            @PathVariable YogurtBatch.BatchStatus status) {
        return ResponseEntity.ok(batchRepository.findByStatus(status));
    }

    @GetMapping("/recipe/{recipeId}")
    @Operation(summary = "Obtener lotes por receta",
        description = "Retorna todos los lotes de producción que fueron elaborados usando una receta específica")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de lotes asociados a la receta",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = YogurtBatch.class))),
        @ApiResponse(responseCode = "404", description = "No se encontraron lotes para la receta indicada", content = @Content)
    })
    public ResponseEntity<List<YogurtBatch>> getBatchesByRecipe(
            @Parameter(description = "ID de la receta", required = true, example = "1")
            @PathVariable Long recipeId) {
        return ResponseEntity.ok(batchRepository.findByRecipeId(recipeId));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Actualizar estado del lote",
        description = "Cambia el estado actual de un lote de producción. El estado debe seguir el flujo: PREPARING → HEATING → INNOCULATION → INCUBATING → COOLING → REFRIGERATING → COMPLETED")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estado del lote actualizado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = YogurtBatch.class))),
        @ApiResponse(responseCode = "404", description = "Lote no encontrado con el ID proporcionado", content = @Content),
        @ApiResponse(responseCode = "400", description = "Estado inválido para la transición actual", content = @Content)
    })
    public ResponseEntity<YogurtBatch> updateBatchStatus(
            @Parameter(description = "ID único del lote", required = true, example = "1")
            @PathVariable Long id) {
        try {
            YogurtBatch batch = yogurtMakingService.getBatchById(id);
            temperatureControlService.startIncubationControl(batch);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/fail")
    @Operation(summary = "Marcar lote como fallido",
        description = "Registra un lote como fallido e indica la razón del fallo para trazabilidad y mejora del proceso")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lote marcado como fallido exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = YogurtBatch.class))),
        @ApiResponse(responseCode = "404", description = "Lote no encontrado con el ID proporcionado", content = @Content),
        @ApiResponse(responseCode = "400", description = "La razón del fallo no puede estar vacía", content = @Content)
    })
    public ResponseEntity<YogurtBatch> failBatch(
            @Parameter(description = "ID único del lote", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody BatchDto.FailRequest request) {
        try {
            YogurtBatch batch = yogurtMakingService.getBatchById(id);
                    batch.setStatus(YogurtBatch.BatchStatus.FAILED);
                    batch.setNotes(request.getReason());
            // Guardar a través del servicio registrando la temperatura final
            yogurtMakingService.logTemperature(id, batch.getCurrentTemperature(),
                com.pedro.demo.domain.model.TemperaturaLog.LogType.MONITORING);
            return ResponseEntity.ok(batch);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
