package com.homie.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.homie.controller.dto.RecipeDtos.CreateRecipeRequest;
import com.homie.model.AppUser;
import com.homie.model.Recipe;
import com.homie.model.Step;
import com.homie.repository.RecipeRepository;
import com.homie.repository.StepRepository;
import com.homie.repository.UserRepository;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final StepRepository stepRepository;
    private final UserRepository userRepository;

    public RecipeService(RecipeRepository recipeRepository, StepRepository stepRepository,
            UserRepository userRepository) {
        this.recipeRepository = recipeRepository;
        this.stepRepository = stepRepository;
        this.userRepository = userRepository;
    }

    /** Saves the recipe with the given user as owner (= creator); steps are numbered in the given order. */
    @Transactional
    public Recipe create(CreateRecipeRequest request, String username) {
        AppUser owner = userRepository.findByUsername(username).orElseThrow();
        String description = request.description() == null ? "" : request.description().trim();
        int preparationTime = request.preparationTime() == null ? 0 : request.preparationTime();

        Recipe recipe = recipeRepository.save(new Recipe(request.name().trim(), description,
                nonBlank(request.ingredients()), preparationTime, owner, Boolean.TRUE.equals(request.shared())));

        List<String> instructions = nonBlank(request.steps());
        List<Step> steps = new ArrayList<>();
        for (int i = 0; i < instructions.size(); i++) {
            steps.add(stepRepository.save(new Step(recipe, i + 1, instructions.get(i))));
        }
        // set directly, so the response contains the steps without reloading the recipe
        recipe.setSteps(steps);
        return recipe;
    }

    /** Trimmed entries without the empty rows the form may send. */
    public static List<String> nonBlank(List<String> values) {
        if (values == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .toList());
    }
}
