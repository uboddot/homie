package com.homie.nutrition;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.homie.model.Ingredient;

/** Entry point of the nutrition module: suggested values for a recipe, or the values a user entered. */
@Service
public class NutritionService {

    private final IngredientNutritionCache cache;
    private final NutritionCalculator calculator;

    public NutritionService(IngredientNutritionCache cache, NutritionCalculator calculator) {
        this.cache = cache;
        this.calculator = calculator;
    }

    /** Macros per portion calculated from the ingredients (suggested = true), null if nothing is known. */
    public Nutrition suggest(List<Ingredient> ingredients, int portions) {
        return suggest(ingredients, portions, true);
    }

    /** Like {@link #suggest(List, int)}; with useAi false, unknown ingredients aren't asked from the source (Gemini). */
    public Nutrition suggest(List<Ingredient> ingredients, int portions, boolean useAi) {
        return calculator.perPortion(ingredients, portions, cache.lookup(ingredients, useAi));
    }

    /** Macros per portion as entered by a user (suggested = false), with the calories derived. */
    public Nutrition fromUser(BigDecimal fat, BigDecimal carbs, BigDecimal protein) {
        return calculator.nutrition(fat, carbs, protein, false);
    }
}
