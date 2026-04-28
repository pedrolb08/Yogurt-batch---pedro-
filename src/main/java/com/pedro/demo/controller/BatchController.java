package com.pedro.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.pedro.demo.demain.repository.YogurtBatchRepository;
import com.pedro.demo.domain.model.YogurtBatch;
import com.pedro.demo.dto.BatchDto;
import com.pedro.demo.servicio.YogurtMakingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequestMapping("/api/batches")
@RequiredArgsConstructor
@Tag(name = "Lotes de Yogur", description = "Gestión de lotes de producción")
public class BatchController {

    private final YogurtBatchRepository batchRepository;
    private final YogurtMakingService yogurtMakingService;

    @GetMapping
    @Operation(summary = "Obtener todos los lotes")
    public ResponseEntity<List<YogurtBatch>> getAllBatches() {
        return ResponseEntity.ok(batchRepository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener lote por ID")
    public ResponseEntity<YogurtBatch> getBatchById(@PathVariable Long id) {
        return batchRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/start")
    @Operation(summary = "Iniciar nuevo lote de yogur")
    public ResponseEntity<YogurtBatch> startBatch(@RequestBody BatchDto.StartBatchRequest request) {
        YogurtBatch batch = yogurtMakingService.startBatch(
                request.getRecipeId(),
                request.getCustomMilkVolume(),
                request.getCustomStarterAmount()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(batch);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Obtener lotes por estado")
    public ResponseEntity<List<YogurtBatch>> getBatchesByStatus(@PathVariable YogurtBatch.BatchStatus status) {
        return ResponseEntity.ok(batchRepository.findByStatus(status));
    }

    @GetMapping("/recipe/{recipeId}")
    @Operation(summary = "Obtener lotes por receta")
    public ResponseEntity<List<YogurtBatch>> getBatchesByRecipe(@PathVariable Long recipeId) {
        return ResponseEntity.ok(batchRepository.findByRecipeId(recipeId));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Actualizar estado del lote")
    public ResponseEntity<YogurtBatch> updateBatchStatus(@PathVariable Long id, @RequestParam YogurtBatch.BatchStatus status) {
        return batchRepository.findById(id)
                .map(batch -> {
                    batch.setStatus(status);
                    return ResponseEntity.ok(batchRepository.save(batch));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/fail")
    @Operation(summary = "Marcar lote como fallido")
    public ResponseEntity<YogurtBatch> failBatch(@PathVariable Long id, @RequestBody BatchDto.FailRequest request) {
        return batchRepository.findById(id)
                .map(batch -> {
                    batch.setStatus(YogurtBatch.BatchStatus.FAILED);
                    batch.setNotes(request.getReason());
                    return ResponseEntity.ok(batchRepository.save(batch));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/count/{status}")
    @Operation(summary = "Contar lotes por estado")
    public ResponseEntity<Long> countBatchesByStatus(@PathVariable YogurtBatch.BatchStatus status) {
        return ResponseEntity.ok(batchRepository.countByStatus(status));
    }
}
