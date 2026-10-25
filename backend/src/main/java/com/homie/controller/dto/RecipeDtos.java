package com.homie.controller.dto;

import java.math.BigDecimal;
import java.util.List;

import com.homie.model.IngredientUnit;

public final class RecipeDtos {

    private RecipeDtos() {
    }

    /** New or edited recipe from the frontend; the owner is always the logged-in user, never part of the request. */
    public record CreateRecipeRequest(String name, String description, Integer preparationTime, Integer portions,
            List<IngredientRequest> ingredients, List<String> steps, Boolean shared) {
    }

    /** One ingredient row; amount and unit may be null. Unknown units are rejected by JSON parsing (400). */
    public record IngredientRequest(BigDecimal amount, IngredientUnit unit, String name) {
    }
}
