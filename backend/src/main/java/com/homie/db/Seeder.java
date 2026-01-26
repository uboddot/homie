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
        recipeRepository.save(new Recipe(null, "spaghetti bolognese", "Classic Italian pasta dish",
                List.of("spaghetti", "ground beef", "tomato sauce", "onion", "garlic"), 45));
        recipeRepository.save(new Recipe(null, "chocolate chip cookies", "Crispy on the outside, chewy on the inside",
                List.of("flour", "sugar", "butter", "eggs", "chocolate chips"), 30));
        recipeRepository.save(new Recipe(null, "caesar salad", "Fresh and crunchy salad with creamy dressing",
                List.of("romaine lettuce", "croutons", "parmesan cheese", "caesar dressing"), 15));
        recipeRepository.save(new Recipe(null, "chicken curry", "Spicy and flavorful chicken curry",
                List.of("chicken", "curry powder", "coconut milk", "onion", "garlic", "ginger"), 50));
    }
}
