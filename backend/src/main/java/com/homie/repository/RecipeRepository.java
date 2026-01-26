package com.homie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.homie.model.Recipe;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

}
