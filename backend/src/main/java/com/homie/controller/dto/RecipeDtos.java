package com.homie.controller.dto;

import java.util.List;

public final class RecipeDtos {

    private RecipeDtos() {
    }

    /** New or edited recipe from the frontend; the owner is always the logged-in user, never part of the request. */
    public record CreateRecipeRequest(String name, String description, Integer preparationTime,
            List<String> ingredients, List<String> steps, Boolean shared) {
    }
}
