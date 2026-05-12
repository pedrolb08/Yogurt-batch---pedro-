package com.pedro.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.pedro.demo.domain.model.Recipe;
import com.pedro.demo.dto.RecipeDto;
import com.pedro.demo.servicio.RecipeService;
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

    private final RecipeService recipeService;

    @GetMapping
    @Operation(summary = "Obtener todas las recetas",
        description = "Retorna la lista completa de recetas registradas en el sistema, incluyendo activas e inactivas")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de recetas obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Recipe.class))),
        @ApiResponse(responseCode = "404", description = "No se encontraron recetas", content = @Content)
    })
    public ResponseEntity<List<Recipe>> getAllRecipes() {
        return ResponseEntity.ok(recipeService.getAllActiveRecipes());
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
        try {
            return ResponseEntity.ok(recipeService.getRecipe(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
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
        Recipe savedRecipe = recipeService.createRecipe(recipeDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRecipe);
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
        try {
            Recipe updatedRecipe = recipeService.updateRecipe(id, recipeDto);
            return ResponseEntity.ok(updatedRecipe);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar receta",
        description = "Desactiva una receta del sistema usando su ID (soft delete)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Receta desactivada exitosamente", content = @Content),
        @ApiResponse(responseCode = "404", description = "Receta no encontrada con el ID proporcionado", content = @Content)
    })
    public ResponseEntity<Void> deleteRecipe(
            @Parameter(description = "ID único de la receta a desactivar", required = true, example = "1")
            @PathVariable Long id) {
        try {
            recipeService.deactivateRecipe(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
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
        return ResponseEntity.ok(recipeService.getAllActiveRecipes());
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar recetas por palabra clave",
        description = "Busca recetas por nombre o descripción usando una palabra clave")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de recetas encontradas",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Recipe.class))),
        @ApiResponse(responseCode = "400", description = "Palabra clave vacía", content = @Content)
    })
    public ResponseEntity<List<Recipe>> searchRecipes(
            @Parameter(description = "Palabra clave para buscar", required = true, example = "natural")
            @RequestParam String keyword) {
        return ResponseEntity.ok(recipeService.searchRecipes(keyword));
    }
}
