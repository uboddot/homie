package com.homie.nutrition;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientNutritionRepository extends JpaRepository<IngredientNutrition, Long> {

    List<IngredientNutrition> findByNameIn(Collection<String> names);
}
