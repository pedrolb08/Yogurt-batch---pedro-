package com.pedro.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.pedro.demo.demain.repository.RecipeRepository;
import com.pedro.demo.domain.model.Recipe;
import com.pedro.demo.dto.RecipeDto;
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
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
@Tag(name = "Gestión de Recetas", description = "Operaciones para crear, consultar, actualizar y eliminar recetas de yogur artesanal")
public class RecipeController {

    private final RecipeRepository recipeRepository;

    @GetMapping
    @Operation(summary = "Obtener todas las recetas",
        description = "Retorna la lista completa de recetas registradas en el sistema, incluyendo activas e inactivas")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de recetas obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Recipe.class))),
        @ApiResponse(responseCode = "404", description = "No se encontraron recetas", content = @Content)
    })
    public ResponseEntity<List<Recipe>> getAllRecipes() {
        return ResponseEntity.ok(recipeRepository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener receta por ID",
        description = "Busca y retorna una receta específica usando su identificador único")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Receta encontrada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Recipe.class))),
        @ApiResponse(responseCode = "404", description = "Receta no encontrada con el ID proporcionado", content = @Content)
    })
    public ResponseEntity<Recipe> getRecipeById(
            @Parameter(description = "ID único de la receta", required = true, example = "1")
            @PathVariable Long id) {
        return recipeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nueva receta",
        description = "Registra una nueva receta de yogur con todos sus parámetros de temperatura, tiempos e ingredientes")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Receta creada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Recipe.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o campos obligatorios vacíos", content = @Content)
    })
    public ResponseEntity<Recipe> createRecipe(@RequestBody RecipeDto recipeDto) {
        Recipe recipe = Recipe.builder()
                .name(recipeDto.getName())
                .description(recipeDto.getDescription())
                .defaultMilkVolume(recipeDto.getDefaultMilkVolume())
                .defaultStarterAmount(recipeDto.getDefaultStarterAmount())
                .heatingTemperature(recipeDto.getHeatingTemperature())
                .heatingDuration(recipeDto.getHeatingDuration())
                .innoculationTemperature(recipeDto.getInnoculationTemperature())
                .incubationTemperature(recipeDto.getIncubationTemperature())
                .minIncubationTime(recipeDto.getMinIncubationTime())
                .maxIncubationTime(recipeDto.getMaxIncubationTime())
                .refrigerationTime(recipeDto.getRefrigerationTime())
                .difficulty(recipeDto.getDifficulty())
                .tips(recipeDto.getTips())
                .active(true)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(recipeRepository.save(recipe));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar receta existente",
        description = "Modifica los datos de una receta ya registrada. Todos los campos serán reemplazados con los nuevos valores")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Receta actualizada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Recipe.class))),
        @ApiResponse(responseCode = "404", description = "Receta no encontrada con el ID proporcionado", content = @Content),
        @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud", content = @Content)
    })
    public ResponseEntity<Recipe> updateRecipe(
            @Parameter(description = "ID único de la receta a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody RecipeDto recipeDto) {
        return recipeRepository.findById(id)
                .map(recipe -> {
                    recipe.setName(recipeDto.getName());
                    recipe.setDescription(recipeDto.getDescription());
                    recipe.setDefaultMilkVolume(recipeDto.getDefaultMilkVolume());
                    recipe.setDefaultStarterAmount(recipeDto.getDefaultStarterAmount());
                    recipe.setHeatingTemperature(recipeDto.getHeatingTemperature());
                    recipe.setHeatingDuration(recipeDto.getHeatingDuration());
                    recipe.setInnoculationTemperature(recipeDto.getInnoculationTemperature());
                    recipe.setIncubationTemperature(recipeDto.getIncubationTemperature());
                    recipe.setMinIncubationTime(recipeDto.getMinIncubationTime());
                    recipe.setMaxIncubationTime(recipeDto.getMaxIncubationTime());
                    recipe.setRefrigerationTime(recipeDto.getRefrigerationTime());
                    recipe.setDifficulty(recipeDto.getDifficulty());
                    recipe.setTips(recipeDto.getTips());
                    return ResponseEntity.ok(recipeRepository.save(recipe));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar receta",
        description = "Elimina permanentemente una receta del sistema usando su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Receta eliminada exitosamente", content = @Content),
        @ApiResponse(responseCode = "404", description = "Receta no encontrada con el ID proporcionado", content = @Content)
    })
    public ResponseEntity<Void> deleteRecipe(
            @Parameter(description = "ID único de la receta a eliminar", required = true, example = "1")
            @PathVariable Long id) {
        if (recipeRepository.existsById(id)) {
            recipeRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/active")
    @Operation(summary = "Obtener recetas activas",
        description = "Retorna únicamente las recetas marcadas como activas y disponibles para producción")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de recetas activas obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Recipe.class))),
        @ApiResponse(responseCode = "404", description = "No se encontraron recetas activas", content = @Content)
    })
    public ResponseEntity<List<Recipe>> getActiveRecipes() {
        return ResponseEntity.ok(recipeRepository.findByActive(true));
    }

    @GetMapping("/difficulty/{difficulty}")
    @Operation(summary = "Obtener recetas por nivel de dificultad",
        description = "Filtra las recetas según su nivel de dificultad: EASY, MEDIUM, DIFFICULT o EXPERT")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de recetas filtradas por dificultad",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Recipe.class))),
        @ApiResponse(responseCode = "400", description = "Nivel de dificultad inválido. Valores permitidos: EASY, MEDIUM, DIFFICULT, EXPERT", content = @Content)
    })
    public ResponseEntity<List<Recipe>> getRecipesByDifficulty(
            @Parameter(description = "Nivel de dificultad", required = true, example = "EASY")
            @PathVariable Recipe.DifficultyLevel difficulty) {
        return ResponseEntity.ok(recipeRepository.findByDifficulty(difficulty));
    }
}
