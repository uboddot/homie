package com.homie.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.homie.controller.dto.RecipeDtos.CreateRecipeRequest;
import com.homie.controller.dto.RecipeDtos.IngredientRequest;
import com.homie.controller.dto.RecipeDtos.NutritionRequest;
import com.homie.model.AppUser;
import com.homie.model.Ingredient;
import com.homie.model.Recipe;
import com.homie.model.Step;
import com.homie.nutrition.Nutrition;
import com.homie.nutrition.NutritionService;
import com.homie.repository.RecipeRepository;
import com.homie.repository.StepRepository;
import com.homie.repository.UserRepository;

@Service
public class RecipeService {

    /** Used when a request doesn't say how many portions the recipe makes. */
    public static final int DEFAULT_PORTIONS = 4;

    private final RecipeRepository recipeRepository;
    private final StepRepository stepRepository;
    private final UserRepository userRepository;
    private final NutritionService nutritionService;
    private final UserSettingsService settingsService;

    public RecipeService(RecipeRepository recipeRepository, StepRepository stepRepository,
            UserRepository userRepository, NutritionService nutritionService, UserSettingsService settingsService) {
        this.recipeRepository = recipeRepository;
        this.stepRepository = stepRepository;
        this.userRepository = userRepository;
        this.nutritionService = nutritionService;
        this.settingsService = settingsService;
    }

    /**
     * Saves the recipe with the given user as owner (= creator); steps are numbered in the given order.
     * Without nutrition in the request, it is suggested from the ingredients (Gemini only if the user allows AI).
     */
    @Transactional
    public Recipe create(CreateRecipeRequest request, String username) {
        // the cookie can outlive the user, e.g. after a dev restart recreated the DB
        AppUser owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in again"));
        String description = request.description() == null ? "" : request.description().trim();
        int preparationTime = request.preparationTime() == null ? 0 : request.preparationTime();

        List<Ingredient> ingredients = ingredients(request.ingredients());
        int portions = portions(request);
        Recipe recipe = new Recipe(request.name().trim(), description, ingredients, preparationTime, portions, owner,
                Boolean.TRUE.equals(request.shared()));
        recipe.setNutrition(request.nutrition() != null
                ? fromUser(request.nutrition())
                : nutritionService.suggest(ingredients, portions, settingsService.useAi(username)));
        recipe = recipeRepository.save(recipe);

        saveSteps(recipe, nonBlank(request.steps()));
        return recipe;
    }

    /**
     * Overwrites all fields and replaces the steps. Only the owner may edit a recipe:
     * unknown id → 404, someone else's recipe → 403. Nutrition from the request is taken as the user's; without it,
     * an unchanged suggestion is kept and otherwise suggested again (values a user entered are only kept if sent back).
     */
    @Transactional
    public Recipe update(Long id, CreateRecipeRequest request, String username) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
        if (!username.equals(recipe.getOwnerUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only edit your own recipes");
        }

        List<Ingredient> ingredients = ingredients(request.ingredients());
        int portions = portions(request);
        Nutrition current = recipe.getNutrition();
        boolean keepSuggestion = current != null && Boolean.TRUE.equals(current.getSuggested())
                && portions == recipe.getPortions() && sameIngredients(recipe.getIngredients(), ingredients);
        if (request.nutrition() != null) {
            recipe.setNutrition(fromUser(request.nutrition()));
        } else if (!keepSuggestion) {
            recipe.setNutrition(nutritionService.suggest(ingredients, portions, settingsService.useAi(username)));
        }

        recipe.setName(request.name().trim());
        recipe.setDescription(request.description() == null ? "" : request.description().trim());
        recipe.setIngredients(ingredients);
        recipe.setPreparationTime(request.preparationTime() == null ? 0 : request.preparationTime());
        recipe.setPortions(portions);
        recipe.setShared(Boolean.TRUE.equals(request.shared()));

        stepRepository.deleteAll(recipe.getSteps());
        saveSteps(recipe, nonBlank(request.steps()));
        return recipe;
    }

    private static int portions(CreateRecipeRequest request) {
        return request.portions() == null ? DEFAULT_PORTIONS : request.portions();
    }

    private Nutrition fromUser(NutritionRequest nutrition) {
        return nutritionService.fromUser(nutrition.fat(), nutrition.carbs(), nutrition.protein());
    }

    /** Same names, units and amounts in the same order; amounts are compared by value (2 equals 2.000). */
    private static boolean sameIngredients(List<Ingredient> a, List<Ingredient> b) {
        if (a == null || a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            Ingredient x = a.get(i);
            Ingredient y = b.get(i);
            boolean sameAmount = x.getAmount() == null ? y.getAmount() == null
                    : y.getAmount() != null && x.getAmount().compareTo(y.getAmount()) == 0;
            if (!sameAmount || x.getUnit() != y.getUnit() || !Objects.equals(x.getName(), y.getName())) {
                return false;
            }
        }
        return true;
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

    /** Ingredients with trimmed names, without the completely empty rows the form may send. */
    public static List<Ingredient> ingredients(List<IngredientRequest> values) {
        if (values == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(values.stream()
                .filter(value -> value != null)
                .map(value -> new Ingredient(value.amount(), value.unit(),
                        value.name() == null ? "" : value.name().trim()))
                .filter(i -> i.getAmount() != null || i.getUnit() != null || !i.getName().isEmpty())
                .toList());
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
