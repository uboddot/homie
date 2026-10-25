package com.homie.nutrition;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Macros of one portion of a recipe, in grams. Each value may be null if it couldn't be calculated.
 * Create it through {@link NutritionCalculator#nutrition}, which derives the calories.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Nutrition {

    @Column(precision = 7, scale = 1)
    private BigDecimal fat;

    @Column(precision = 7, scale = 1)
    private BigDecimal carbs;

    @Column(precision = 7, scale = 1)
    private BigDecimal protein;

    private Integer calories; // kcal, only set if fat, carbs and protein are all known

    private Boolean suggested; // calculated by the backend and not yet edited by a user
}
