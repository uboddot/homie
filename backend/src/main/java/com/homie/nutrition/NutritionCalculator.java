package com.homie.nutrition;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;

import com.homie.model.Ingredient;
import com.homie.model.IngredientUnit;

/** The nutrition math: macros per portion from per-gram values, and calories from the macros. */
@Service
public class NutritionCalculator {

    private static final BigDecimal KCAL_PER_GRAM_FAT = BigDecimal.valueOf(9);
    private static final BigDecimal KCAL_PER_GRAM_CARBS = BigDecimal.valueOf(4);
    private static final BigDecimal KCAL_PER_GRAM_PROTEIN = BigDecimal.valueOf(4);
    private static final BigDecimal GRAMS_PER_KG = BigDecimal.valueOf(1000);

    /**
     * Fat, carbs and protein of one portion, marked as suggested. Ingredients without an amount ("salt") are
     * ignored. A macro is null if it is unknown for any measured ingredient; null if nothing could be calculated.
     */
    public Nutrition perPortion(List<Ingredient> ingredients, int portions, IngredientNutritionCache.Lookup lookup) {
        List<Ingredient> measured = ingredients.stream().filter(i -> i.getAmount() != null).toList();
        if (measured.isEmpty()) {
            return null;
        }

        BigDecimal fat = BigDecimal.ZERO;
        BigDecimal carbs = BigDecimal.ZERO;
        BigDecimal protein = BigDecimal.ZERO;
        for (Ingredient ingredient : measured) {
            BigDecimal grams = grams(ingredient, lookup);
            IngredientNutrition perGram = lookup.nutrition(ingredient);
            fat = add(fat, grams, perGram == null ? null : perGram.getFatPerGram());
            carbs = add(carbs, grams, perGram == null ? null : perGram.getCarbsPerGram());
            protein = add(protein, grams, perGram == null ? null : perGram.getProteinPerGram());
        }
        if (fat == null && carbs == null && protein == null) {
            return null;
        }

        BigDecimal count = BigDecimal.valueOf(portions);
        return nutrition(divide(fat, count), divide(carbs, count), divide(protein, count), true);
    }

    /** Nutrition with the macros rounded to 0.1 g and the calories derived from them. */
    public Nutrition nutrition(BigDecimal fat, BigDecimal carbs, BigDecimal protein, boolean suggested) {
        fat = round(fat);
        carbs = round(carbs);
        protein = round(protein);
        return new Nutrition(fat, carbs, protein, calories(fat, carbs, protein), suggested);
    }

    /** kcal = 9 per g fat + 4 per g carbs + 4 per g protein; null unless all three are known. */
    public Integer calories(BigDecimal fat, BigDecimal carbs, BigDecimal protein) {
        if (fat == null || carbs == null || protein == null) {
            return null;
        }
        return fat.multiply(KCAL_PER_GRAM_FAT)
                .add(carbs.multiply(KCAL_PER_GRAM_CARBS))
                .add(protein.multiply(KCAL_PER_GRAM_PROTEIN))
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();
    }

    /** Weight of the ingredient in grams, null if its unit weight is unknown. */
    private static BigDecimal grams(Ingredient ingredient, IngredientNutritionCache.Lookup lookup) {
        if (ingredient.getUnit() == IngredientUnit.G) {
            return ingredient.getAmount();
        }
        if (ingredient.getUnit() == IngredientUnit.KG) {
            return ingredient.getAmount().multiply(GRAMS_PER_KG);
        }
        BigDecimal gramsPerUnit = lookup.gramsPerUnit(ingredient);
        return gramsPerUnit == null ? null : ingredient.getAmount().multiply(gramsPerUnit);
    }

    /** sum + grams × perGram; once anything is unknown, the sum stays unknown (null). */
    private static BigDecimal add(BigDecimal sum, BigDecimal grams, BigDecimal perGram) {
        return sum == null || grams == null || perGram == null ? null : sum.add(grams.multiply(perGram));
    }

    private static BigDecimal divide(BigDecimal value, BigDecimal divisor) {
        return value == null ? null : value.divide(divisor, 6, RoundingMode.HALF_UP);
    }

    private static BigDecimal round(BigDecimal value) {
        return value == null ? null : value.setScale(1, RoundingMode.HALF_UP);
    }
}
