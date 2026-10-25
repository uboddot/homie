package com.homie.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

        saveSteps(recipe, nonBlank(request.steps()));
        return recipe;
    }

    /**
     * Overwrites all fields and replaces the steps. Only the owner may edit a recipe:
     * unknown id → 404, someone else's recipe → 403.
     */
    @Transactional
    public Recipe update(Long id, CreateRecipeRequest request, String username) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
        if (!username.equals(recipe.getOwnerUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only edit your own recipes");
        }

        recipe.setName(request.name().trim());
        recipe.setDescription(request.description() == null ? "" : request.description().trim());
        recipe.setIngredients(nonBlank(request.ingredients()));
        recipe.setPreparationTime(request.preparationTime() == null ? 0 : request.preparationTime());
        recipe.setShared(Boolean.TRUE.equals(request.shared()));

        stepRepository.deleteAll(recipe.getSteps());
        saveSteps(recipe, nonBlank(request.steps()));
        return recipe;
    }

    /** Saves the steps numbered in the given order. */
    private void saveSteps(Recipe recipe, List<String> instructions) {
        List<Step> steps = new ArrayList<>();
        for (int i = 0; i < instructions.size(); i++) {
            steps.add(stepRepository.save(new Step(recipe, i + 1, instructions.get(i))));
        }
        // set directly, so the response contains the steps without reloading the recipe
        recipe.setSteps(steps);
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
