package com.homie.model;

import java.math.BigDecimal;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A per-meal target: either a single goal, or a range where one side alone means "at least" / "at most". */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealTarget {

    private BigDecimal goal;

    private BigDecimal min;

    private BigDecimal max;
}
