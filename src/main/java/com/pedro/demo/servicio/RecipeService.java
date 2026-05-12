package com.pedro.demo.servicio;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pedro.demo.domain.model.Ingredient;
import com.pedro.demo.domain.model.Recipe;
import com.pedro.demo.demain.repository.RecipeRepository;
import com.pedro.demo.dto.RecipeDto;
import com.pedro.demo.dto.IngredientDto;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {
    
    private final RecipeRepository recipeRepository;
    
    @Transactional
    public Recipe createRecipe(RecipeDto recipeDto) {
        if (recipeRepository.findByName(recipeDto.getName()).isPresent()) {
            throw new RuntimeException("Recipe with name '" + recipeDto.getName() + "' already exists");
        }
        
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
        
        // Agregar ingredientes
        if (recipeDto.getIngredients() != null) {
            recipeDto.getIngredients().forEach(ingredientDto -> {
                Ingredient ingredient = Ingredient.builder()
                    .name(ingredientDto.getName())
                    .quantity(ingredientDto.getQuantity())
                    .unit(ingredientDto.getUnit())
                    .notes(ingredientDto.getNotes())
                    .optional(ingredientDto.getOptional())
                    .recipe(recipe)
                    .build();
                recipe.getIngredients().add(ingredient);
            });
        }
        
        Recipe savedRecipe = recipeRepository.save(recipe);
        log.info("Recipe created: {}", savedRecipe.getName());
        
        return savedRecipe;
    }
    
    @Transactional
    public Recipe updateRecipe(Long id, RecipeDto recipeDto) {
        Recipe recipe = getRecipe(id);
        
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
        
        // Actualizar ingredientes (simplificado - en producción se manejaría mejor)
        recipe.getIngredients().clear();
        if (recipeDto.getIngredients() != null) {
            recipeDto.getIngredients().forEach(ingredientDto -> {
                Ingredient ingredient = Ingredient.builder()
                    .name(ingredientDto.getName())
                    .quantity(ingredientDto.getQuantity())
                    .unit(ingredientDto.getUnit())
                    .notes(ingredientDto.getNotes())
                    .optional(ingredientDto.getOptional())
                    .recipe(recipe)
                    .build();
                recipe.getIngredients().add(ingredient);
            });
        }
        
        Recipe updatedRecipe = recipeRepository.save(recipe);
        log.info("Recipe updated: {}", updatedRecipe.getName());
        
        return updatedRecipe;
    }
    
    public Recipe getRecipe(Long id) {
        return recipeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Recipe not found with id: " + id));
    }
    
    public List<Recipe> getAllActiveRecipes() {
        return recipeRepository.findByActive(true);
    }
    
    public List<Recipe> searchRecipes(String keyword) {
        return recipeRepository.searchByKeyword(keyword);
    }
    
    @Transactional
    public void deactivateRecipe(Long id) {
        Recipe recipe = getRecipe(id);
        recipe.setActive(false);
        recipeRepository.save(recipe);
        log.info("Recipe deactivated: {}", recipe.getName());
    }
    
    @Transactional
    public void activateRecipe(Long id) {
        Recipe recipe = getRecipe(id);
        recipe.setActive(true);
        recipeRepository.save(recipe);
        log.info("Recipe activated: {}", recipe.getName());
    }
}