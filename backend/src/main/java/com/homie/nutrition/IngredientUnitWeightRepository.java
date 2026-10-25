package com.homie.nutrition;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientUnitWeightRepository extends JpaRepository<IngredientUnitWeight, Long> {

    List<IngredientUnitWeight> findByNameIn(Collection<String> names);
}
