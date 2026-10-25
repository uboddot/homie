package com.homie.nutrition;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Cached macros of an ingredient, e.g. potato: 1 g has 0.17 g carbs. A value is null if it is unknown. */
@Entity
@Table(name = "ingredient_nutrition")
@Data
@NoArgsConstructor
public class IngredientNutrition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // normalized, see IngredientNutritionCache.normalize

    @Column(precision = 10, scale = 6)
    private BigDecimal fatPerGram;

    @Column(precision = 10, scale = 6)
    private BigDecimal carbsPerGram;

    @Column(precision = 10, scale = 6)
    private BigDecimal proteinPerGram;

    public IngredientNutrition(String name, BigDecimal fatPerGram, BigDecimal carbsPerGram, BigDecimal proteinPerGram) {
        this.name = name;
        this.fatPerGram = fatPerGram;
        this.carbsPerGram = carbsPerGram;
        this.proteinPerGram = proteinPerGram;
    }
}
