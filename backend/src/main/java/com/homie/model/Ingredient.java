package com.homie.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One row of a recipe's ingredient list, e.g. 250 g flour. Amount and unit are optional ("salt"). */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient {

    @Column(precision = 10, scale = 3)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private IngredientUnit unit;

    private String name;
}
