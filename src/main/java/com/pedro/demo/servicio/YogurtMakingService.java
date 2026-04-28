package com.pedro.demo.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.pedro.demo.demain.repository.RecipeRepository;
import com.pedro.demo.demain.repository.YogurtBatchRepository;
import com.pedro.demo.demain.repository.TemperaturaLogRepository;
import com.pedro.demo.domain.model.Recipe;
import com.pedro.demo.domain.model.YogurtBatch;
import com.pedro.demo.domain.model.TemperaturaLog;
import java.util.List;

@Service
public class YogurtMakingService {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private YogurtBatchRepository yougurtBatchRepository;

    @Autowired
    private TemperaturaLogRepository temperaturaLogRepository;

    public YogurtBatch startBatch(Long recipeId, Double milkVolume, Double starterAmount) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Receta no encontrada"));

        YogurtBatch batch = YogurtBatch.builder()
                .batchCode("BATCH-" + System.currentTimeMillis())
                .recipe(recipe)
                .milkVolume(milkVolume != null ? milkVolume : recipe.getDefaultMilkVolume())
                .starterAmount(starterAmount != null ? starterAmount : recipe.getDefaultStarterAmount())
                .status(YogurtBatch.BatchStatus.PREPARING)
                .build();

        return yougurtBatchRepository.save(batch);
    }

    public void logTemperature(Long batchId, Double temperature, TemperaturaLog.LogType type) {
        YogurtBatch batch = yougurtBatchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Lote no encontrado"));

        TemperaturaLog log = TemperaturaLog.builder()
                .batch(batch)
                .temperature(temperature)
                .type(type)
                .build();

        temperaturaLogRepository.save(log);
    }

    public List<YogurtBatch> getBatchesByStatus(YogurtBatch.BatchStatus status) {
        return yougurtBatchRepository.findByStatus(status);
    }

    public YogurtBatch getBatchById(Long batchId) {
        return yougurtBatchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Lote no encontrado"));
    }
}

