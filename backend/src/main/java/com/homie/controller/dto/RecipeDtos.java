package com.homie.controller.dto;

import java.math.BigDecimal;
import java.util.List;

import com.homie.model.IngredientUnit;

public final class RecipeDtos {

    private RecipeDtos() {
    }

    /**
     * New or edited recipe from the frontend; the owner is always the logged-in user, never part of the request.
     * Without nutrition the backend suggests it.
     */
    public record CreateRecipeRequest(String name, String description, Integer preparationTime, Integer portions,
            List<IngredientRequest> ingredients, List<String> steps, Boolean shared, NutritionRequest nutrition) {
    }

    /** One ingredient row; amount and unit may be null. Unknown units are rejected by JSON parsing (400). */
    public record IngredientRequest(BigDecimal amount, IngredientUnit unit, String name) {
    }

    /** Macros per portion in grams, entered by a user; the calories are always derived by the backend. */
    public record NutritionRequest(BigDecimal fat, BigDecimal carbs, BigDecimal protein) {
    }
}
