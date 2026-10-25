package com.homie.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.homie.controller.dto.AuthDtos.ErrorResponse;
import com.homie.controller.dto.RecipeDtos.CreateRecipeRequest;
import com.homie.repository.RecipeRepository;
import com.homie.services.RecipeService;
import com.homie.model.Ingredient;
import com.homie.model.Recipe;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/recipes")
public class RecipeController {

    private final RecipeRepository recipeRepository;
    private final RecipeService recipeService;

    public RecipeController(RecipeRepository recipeRepository, RecipeService recipeService) {
        this.recipeRepository = recipeRepository;
        this.recipeService = recipeService;
    }

    @GetMapping
    public ResponseEntity<List<Recipe>> getRecipes(Authentication authentication) {
        return new ResponseEntity<List<Recipe>>(recipeRepository.findVisibleFor(authentication.getName()), HttpStatus.OK);
    }

    @GetMapping("/getRecipeById")
    public ResponseEntity<Recipe> getRecipeById(@RequestParam Long recipeId) {
        return new ResponseEntity<Recipe>(recipeRepository.findById(recipeId).orElse(null), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> createRecipe(@RequestBody CreateRecipeRequest request, Authentication authentication) {
        ResponseEntity<?> invalid = validate(request);
        if (invalid != null) {
            return invalid;
        }

        Recipe recipe = recipeService.create(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(recipe);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRecipe(@PathVariable Long id, @RequestBody CreateRecipeRequest request,
            Authentication authentication) {
        ResponseEntity<?> invalid = validate(request);
        if (invalid != null) {
            return invalid;
        }

        return ResponseEntity.ok(recipeService.update(id, request, authentication.getName()));
    }

    /** 400 with a message if the request can't be saved, otherwise null. */
    private static ResponseEntity<?> validate(CreateRecipeRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Name is required"));
        }
        if (request.preparationTime() != null && request.preparationTime() < 0) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Preparation time can't be negative"));
        }
        List<Ingredient> ingredients = RecipeService.ingredients(request.ingredients());
        if (ingredients.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Add at least one ingredient"));
        }
        for (Ingredient ingredient : ingredients) {
            if (ingredient.getAmount() != null && ingredient.getAmount().signum() <= 0) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Amounts must be positive"));
            }
            if (ingredient.getUnit() != null && ingredient.getAmount() == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("A unit needs an amount"));
            }
            if (ingredient.getName().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Every amount needs an ingredient"));
            }
        }
        return null;
    }
}
