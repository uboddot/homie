package com.homie.nutrition;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.homie.model.IngredientUnit;

/** Cached weight of one unit of an ingredient, e.g. 1 PIECE potato = 170 g. Not needed for G and KG. */
@Entity
@Table(name = "ingredient_unit_weight", uniqueConstraints = @UniqueConstraint(columnNames = { "name", "unit" }))
@Data
@NoArgsConstructor
public class IngredientUnitWeight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // normalized, see IngredientNutritionCache.normalize

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private IngredientUnit unit;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal grams;

    public IngredientUnitWeight(String name, IngredientUnit unit, BigDecimal grams) {
        this.name = name;
        this.unit = unit;
        this.grams = grams;
    }
}
