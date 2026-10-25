package com.homie.nutrition;

import java.util.List;
import java.util.Set;

import com.homie.model.IngredientUnit;

/** Looks up what the cache doesn't know yet (see {@link GeminiNutritionSource}); tests mock it. */
public interface NutritionSource {

    /** An ingredient (normalized name) in a unit other than G/KG, whose weight per unit is needed. */
    record UnitKey(String name, IngredientUnit unit) {
    }

    /** What the source could answer; anything it didn't know is simply missing. */
    record Fetched(List<IngredientNutrition> ingredients, List<IngredientUnitWeight> unitWeights) {
        public static final Fetched EMPTY = new Fetched(List.of(), List.of());
    }

    /** Macros per gram for the given ingredient names and grams per unit for the given pairs, in one request. */
    Fetched fetch(Set<String> names, Set<UnitKey> unitWeights);
}
