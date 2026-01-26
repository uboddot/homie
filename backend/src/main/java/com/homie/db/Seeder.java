package com.homie.db;

import java.util.List;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import com.homie.model.Recipe;
import com.homie.repository.RecipeRepository;

@Component
public class Seeder {

    private final RecipeRepository recipeRepository;

    public Seeder(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    @PostConstruct
    public void seed() {
        // Seed a sample recipe when the application context starts
        recipeRepository.save(new Recipe(null, "apple pie", "Delicious apple pie",
                List.of("apples", "flour", "sugar", "butter"), 60));
    }
}
