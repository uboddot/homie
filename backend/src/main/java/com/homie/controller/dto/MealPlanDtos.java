package com.homie.controller.dto;

import java.time.LocalDate;
import java.util.List;

import com.homie.model.MealSlot;

public final class MealPlanDtos {

    private MealPlanDtos() {
    }

    /** One planned meal; the recipe name is included so the calendar doesn't need every recipe loaded. */
    public record MealPlanEntryResponse(LocalDate date, MealSlot slot, Long recipeId, String recipeName) {
    }

    /** Sets the slot to the recipe on every given date; recipeId null clears the slot. */
    public record SetMealRequest(List<LocalDate> dates, MealSlot slot, Long recipeId) {
    }
}
