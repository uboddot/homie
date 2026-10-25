package com.homie.db;

import static com.homie.model.IngredientUnit.BUNCH;
import static com.homie.model.IngredientUnit.CAN;
import static com.homie.model.IngredientUnit.CLOVE;
import static com.homie.model.IngredientUnit.DL;
import static com.homie.model.IngredientUnit.G;
import static com.homie.model.IngredientUnit.KG;
import static com.homie.model.IngredientUnit.L;
import static com.homie.model.IngredientUnit.ML;
import static com.homie.model.IngredientUnit.PIECE;
import static com.homie.model.IngredientUnit.PINCH;
import static com.homie.model.IngredientUnit.SLICE;
import static com.homie.model.IngredientUnit.TBSP;
import static com.homie.model.IngredientUnit.TSP;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.homie.model.AppUser;
import com.homie.model.Ingredient;
import com.homie.model.IngredientUnit;
import com.homie.model.MealPlanEntry;
import com.homie.model.MealSlot;
import com.homie.model.Recipe;
import com.homie.model.Step;
import com.homie.nutrition.IngredientNutrition;
import com.homie.nutrition.IngredientNutritionRepository;
import com.homie.nutrition.IngredientUnitWeight;
import com.homie.nutrition.IngredientUnitWeightRepository;
import com.homie.nutrition.NutritionService;
import com.homie.repository.MealPlanEntryRepository;
import com.homie.repository.RecipeRepository;
import com.homie.repository.StepRepository;
import com.homie.repository.UserRepository;
import com.homie.security.ApplicationUserRole;

/** Dev data only: runs with the "dev" Spring profile. */
@Component
@Profile("dev")
public class Seeder {

        private final RecipeRepository recipeRepository;
        private final StepRepository stepRepository;
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final IngredientNutritionRepository ingredientNutritionRepository;
        private final IngredientUnitWeightRepository unitWeightRepository;
        private final NutritionService nutritionService;
        private final MealPlanEntryRepository mealPlanEntryRepository;

        private AppUser anna;
        private AppUser admin;
        private AppUser user;
        private int seededRecipes = 0;

        public Seeder(RecipeRepository recipeRepository, StepRepository stepRepository,
                        UserRepository userRepository, PasswordEncoder passwordEncoder,
                        IngredientNutritionRepository ingredientNutritionRepository,
                        IngredientUnitWeightRepository unitWeightRepository, NutritionService nutritionService,
                        MealPlanEntryRepository mealPlanEntryRepository) {
                this.recipeRepository = recipeRepository;
                this.stepRepository = stepRepository;
                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
                this.ingredientNutritionRepository = ingredientNutritionRepository;
                this.unitWeightRepository = unitWeightRepository;
                this.nutritionService = nutritionService;
                this.mealPlanEntryRepository = mealPlanEntryRepository;
        }

        /** Dev users are verified right away, so they can log in without a mail. */
        private AppUser seedUser(String username, String password, ApplicationUserRole role) {
                AppUser seeded = new AppUser(username, username + "@homie.local", passwordEncoder.encode(password), role);
                seeded.setEmailVerified(true);
                return userRepository.save(seeded);
        }

        /** Alternates the owner between anna and admin and shares every second recipe. */
        private void seedRecipe(Recipe recipe) {
                recipe.setOwner(seededRecipes % 2 == 0 ? anna : admin);
                recipe.setShared(seededRecipes % 4 < 2);
                recipeRepository.save(recipe);
                seededRecipes++;
        }

        /**
         * Private recipe of the given owner, steps are numbered in the given order. Its ingredients are all in the
         * seeded nutrition cache, so the suggestion is calculated without asking Gemini.
         */
        private Recipe seedOwnRecipe(Recipe recipe, AppUser owner, String... steps) {
                recipe.setOwner(owner);
                recipe.setShared(false);
                recipe.setNutrition(nutritionService.suggest(recipe.getIngredients(), recipe.getPortions()));
                recipeRepository.save(recipe);
                for (int i = 0; i < steps.length; i++) {
                        stepRepository.save(new Step(recipe, i + 1, steps[i]));
                }
                return recipe;
        }

        /** Planned meal of user, `days` from today (negative = already eaten). */
        private void seedMeal(int days, MealSlot slot, Recipe recipe) {
                mealPlanEntryRepository.save(new MealPlanEntry(user, LocalDate.now().plusDays(days), slot, recipe));
        }

        private static Ingredient ingredient(String amount, IngredientUnit unit, String name) {
                return new Ingredient(new BigDecimal(amount), unit, name);
        }

        /** Cache row with the macros per 100 g, stored per gram like the ones from Gemini. */
        private void seedIngredientNutrition(String name, String fat, String carbs, String protein) {
                BigDecimal hundred = BigDecimal.valueOf(100);
                ingredientNutritionRepository.save(new IngredientNutrition(name, new BigDecimal(fat).divide(hundred),
                                new BigDecimal(carbs).divide(hundred), new BigDecimal(protein).divide(hundred)));
        }

        private void seedUnitWeight(String name, IngredientUnit unit, String grams) {
                unitWeightRepository.save(new IngredientUnitWeight(name, unit, new BigDecimal(grams)));
        }

        /** Ingredient without amount and unit, e.g. "salt". */
        private static Ingredient ingredient(String name) {
                return new Ingredient(null, null, name);
        }

        @PostConstruct
        public void seed() {
                // Dev users
                anna = seedUser("anna", "password", ApplicationUserRole.USER);
                admin = seedUser("admin", "password123", ApplicationUserRole.ADMIN);
                user = seedUser("user", "password", ApplicationUserRole.USER);

                // Nutrition cache for the ingredients of user's recipes (fat, carbs, protein per 100 g)
                seedIngredientNutrition("apples", "0.2", "14", "0.3");
                seedIngredientNutrition("flour", "1", "76", "10");
                seedIngredientNutrition("sugar", "0", "100", "0");
                seedIngredientNutrition("butter", "81", "0.1", "0.9");
                seedIngredientNutrition("cinnamon", "1.2", "81", "4");
                seedIngredientNutrition("spaghetti", "1.5", "75", "13");
                seedIngredientNutrition("garlic", "0.5", "33", "6.4");
                seedIngredientNutrition("olive oil", "100", "0", "0");
                seedIngredientNutrition("chili flakes", "17", "50", "12");
                seedIngredientNutrition("parsley", "0.8", "6.3", "3");
                seedIngredientNutrition("eggs", "10", "1.1", "13");
                seedIngredientNutrition("tomatoes", "0.3", "4", "1");
                seedIngredientNutrition("bell pepper", "0.3", "6", "1");
                seedIngredientNutrition("onion", "0.1", "9", "1.1");
                seedIngredientNutrition("cumin", "22", "44", "18");
                seedIngredientNutrition("paprika", "13", "54", "14");
                seedUnitWeight("apples", PIECE, "180");
                seedUnitWeight("cinnamon", TSP, "2.6");
                seedUnitWeight("garlic", CLOVE, "4");
                seedUnitWeight("olive oil", TBSP, "13.5");
                seedUnitWeight("chili flakes", PINCH, "0.3");
                seedUnitWeight("parsley", BUNCH, "50");
                seedUnitWeight("eggs", PIECE, "55");
                seedUnitWeight("tomatoes", CAN, "400");
                seedUnitWeight("bell pepper", PIECE, "150");
                seedUnitWeight("onion", PIECE, "110");
                seedUnitWeight("cumin", TSP, "2");
                seedUnitWeight("paprika", TSP, "2.3");

                // user's own private recipes, with steps
                Recipe applePie = seedOwnRecipe(new Recipe("apple pie", "Delicious homemade apple pie",
                                List.of(ingredient("4", PIECE, "apples"), ingredient("250", G, "flour"),
                                                ingredient("100", G, "sugar"), ingredient("125", G, "butter"),
                                                ingredient("1", TSP, "cinnamon")), 60, 8), user,
                                "Preheat oven to 375°F (190°C).",
                                "Rub butter into flour and a pinch of sugar, add cold water and form a dough.",
                                "Peel and slice the apples, toss with sugar and cinnamon.",
                                "Line a pie dish with half the dough, add the apples and cover with the rest.",
                                "Bake for 45 minutes until golden brown.");
                Recipe pasta = seedOwnRecipe(new Recipe("pasta aglio e olio", "Simple spaghetti with garlic and olive oil",
                                List.of(ingredient("200", G, "spaghetti"), ingredient("3", CLOVE, "garlic"),
                                                ingredient("4", TBSP, "olive oil"), ingredient("1", PINCH, "chili flakes"),
                                                ingredient("1", BUNCH, "parsley")), 20, 2), user,
                                "Cook the spaghetti in salted water until al dente.",
                                "Gently fry sliced garlic and chili flakes in olive oil until lightly golden.",
                                "Toss the drained spaghetti in the oil with a splash of pasta water.",
                                "Finish with chopped parsley and serve immediately.");
                Recipe shakshuka = seedOwnRecipe(new Recipe("shakshuka", "Eggs poached in a spiced tomato sauce",
                                List.of(ingredient("4", PIECE, "eggs"), ingredient("1", CAN, "tomatoes"),
                                                ingredient("1", PIECE, "bell pepper"), ingredient("1", PIECE, "onion"),
                                                ingredient("1", TSP, "cumin"), ingredient("1", TSP, "paprika")), 30, 2), user,
                                "Sauté diced onion and bell pepper in a pan until soft.",
                                "Add cumin and paprika, then the chopped tomatoes, and simmer for 10 minutes.",
                                "Make small wells in the sauce and crack an egg into each.",
                                "Cover and cook until the egg whites are set, about 6 minutes.",
                                "Serve hot with bread.");

                // user's meal plan: a few eaten days and some planned ones
                seedMeal(-6, MealSlot.DINNER, pasta);
                seedMeal(-5, MealSlot.BREAKFAST, shakshuka);
                seedMeal(-5, MealSlot.DINNER, applePie);
                seedMeal(-3, MealSlot.BREAKFAST, shakshuka);
                seedMeal(-3, MealSlot.LUNCH, pasta);
                seedMeal(-3, MealSlot.DINNER, pasta);
                seedMeal(-1, MealSlot.LUNCH, pasta);
                seedMeal(0, MealSlot.BREAKFAST, shakshuka);
                seedMeal(0, MealSlot.DINNER, pasta);
                seedMeal(1, MealSlot.DINNER, applePie);
                seedMeal(3, MealSlot.BREAKFAST, shakshuka);
                seedMeal(3, MealSlot.LUNCH, pasta);
                seedMeal(3, MealSlot.DINNER, pasta);

                // Seed a sample recipe when the application context starts
                seedRecipe(new Recipe("spaghetti bolognese", "Classic Italian pasta dish",
                                List.of(ingredient("400", G, "spaghetti"), ingredient("500", G, "ground beef"),
                                                ingredient("500", ML, "tomato sauce"), ingredient("1", PIECE, "onion"),
                                                ingredient("2", CLOVE, "garlic")), 45, 4));
                seedRecipe(new Recipe("chocolate chip cookies", "Crispy on the outside, chewy on the inside",
                                List.of(ingredient("280", G, "flour"), ingredient("200", G, "sugar"),
                                                ingredient("170", G, "butter"), ingredient("2", PIECE, "eggs"),
                                                ingredient("200", G, "chocolate chips")), 30, 24));
                seedRecipe(new Recipe("caesar salad", "Fresh and crunchy salad with creamy dressing",
                                List.of(ingredient("1", PIECE, "romaine lettuce"), ingredient("100", G, "croutons"),
                                                ingredient("50", G, "parmesan cheese"), ingredient("4", TBSP, "caesar dressing")), 15, 2));
                seedRecipe(new Recipe("chicken curry", "Spicy and flavorful chicken curry",
                                List.of(ingredient("600", G, "chicken"), ingredient("2", TBSP, "curry powder"),
                                                ingredient("400", ML, "coconut milk"), ingredient("1", PIECE, "onion"),
                                                ingredient("3", CLOVE, "garlic"), ingredient("1", TBSP, "ginger")), 50, 4));
                seedRecipe(new Recipe("vegetable stir fry", "Colorful veggies with savory sauce",
                                List.of(ingredient("1", PIECE, "broccoli"), ingredient("1", PIECE, "bell pepper"),
                                                ingredient("2", PIECE, "carrot"), ingredient("3", TBSP, "soy sauce"),
                                                ingredient("2", CLOVE, "garlic"), ingredient("1", TSP, "ginger")), 20, 2));
                seedRecipe(new Recipe("beef tacos", "Crispy tacos with seasoned beef",
                                List.of(ingredient("8", PIECE, "taco shells"), ingredient("500", G, "ground beef"),
                                                ingredient("0.5", PIECE, "lettuce"), ingredient("2", PIECE, "tomato"),
                                                ingredient("100", G, "cheddar"), ingredient("2", TBSP, "taco seasoning")), 25, 4));
                seedRecipe(new Recipe("margherita pizza", "Classic pizza with basil and mozzarella",
                                List.of(ingredient("400", G, "pizza dough"), ingredient("150", ML, "tomato sauce"),
                                                ingredient("125", G, "mozzarella"), ingredient("1", BUNCH, "basil"),
                                                ingredient("1", TBSP, "olive oil")), 30, 2));
                seedRecipe(new Recipe("pancakes", "Fluffy breakfast pancakes",
                                List.of(ingredient("200", G, "flour"), ingredient("3", DL, "milk"),
                                                ingredient("2", PIECE, "eggs"), ingredient("2", TSP, "baking powder"),
                                                ingredient("1", TBSP, "sugar"), ingredient("30", G, "butter")), 15, 4));
                seedRecipe(new Recipe("avocado toast", "Simple toast with smashed avocado",
                                List.of(ingredient("2", SLICE, "bread"), ingredient("1", PIECE, "avocado"),
                                                ingredient("0.5", PIECE, "lemon"), ingredient("salt"),
                                                ingredient("pepper"), ingredient("1", PINCH, "chili flakes")), 10, 1));
                seedRecipe(new Recipe("greek salad", "Fresh salad with feta and olives",
                                List.of(ingredient("1", PIECE, "cucumber"), ingredient("3", PIECE, "tomato"),
                                                ingredient("200", G, "feta"), ingredient("100", G, "olives"),
                                                ingredient("0.5", PIECE, "red onion"), ingredient("1", TSP, "oregano")), 12, 2));
                seedRecipe(new Recipe("tomato basil soup", "Comforting soup with herbs",
                                List.of(ingredient("1", KG, "tomatoes"), ingredient("1", PIECE, "onion"),
                                                ingredient("2", CLOVE, "garlic"), ingredient("1", BUNCH, "basil"),
                                                ingredient("5", DL, "vegetable broth")), 35, 4));
                seedRecipe(new Recipe("grilled cheese", "Golden toast with melted cheese",
                                List.of(ingredient("2", SLICE, "bread"), ingredient("2", SLICE, "cheddar"),
                                                ingredient("1", TBSP, "butter")), 10, 1));
                seedRecipe(new Recipe("lemon garlic salmon", "Bright and flaky baked salmon",
                                List.of(ingredient("400", G, "salmon"), ingredient("1", PIECE, "lemon"),
                                                ingredient("2", CLOVE, "garlic"), ingredient("2", TBSP, "olive oil"),
                                                ingredient("1", BUNCH, "dill")), 25, 2));
                seedRecipe(new Recipe("banana smoothie", "Creamy smoothie with banana",
                                List.of(ingredient("2", PIECE, "banana"), ingredient("2", DL, "milk"),
                                                ingredient("150", G, "yogurt"), ingredient("1", TBSP, "honey"),
                                                ingredient("ice")), 5, 2));
                seedRecipe(new Recipe("quinoa bowl", "Healthy bowl with roasted veggies",
                                List.of(ingredient("150", G, "quinoa"), ingredient("1", PIECE, "zucchini"),
                                                ingredient("1", CAN, "chickpeas"), ingredient("2", TBSP, "olive oil"),
                                                ingredient("0.5", PIECE, "lemon"), ingredient("100", G, "spinach")), 30, 2));
                seedRecipe(new Recipe("shrimp scampi", "Garlic butter shrimp over pasta",
                                List.of(ingredient("400", G, "shrimp"), ingredient("4", CLOVE, "garlic"),
                                                ingredient("50", G, "butter"), ingredient("1", BUNCH, "parsley"),
                                                ingredient("1", PIECE, "lemon"), ingredient("300", G, "linguine")), 20, 3));
                seedRecipe(new Recipe("bbq pulled pork", "Slow-cooked pork with smoky sauce",
                                List.of(ingredient("2", KG, "pork shoulder"), ingredient("3", DL, "bbq sauce"),
                                                ingredient("1", PIECE, "onion"), ingredient("2", TBSP, "brown sugar"),
                                                ingredient("1", TBSP, "paprika")), 240, 8));
                seedRecipe(new Recipe("chicken fajitas", "Sizzling chicken with peppers",
                                List.of(ingredient("500", G, "chicken"), ingredient("2", PIECE, "bell peppers"),
                                                ingredient("1", PIECE, "onion"), ingredient("2", TBSP, "fajita seasoning"),
                                                ingredient("8", PIECE, "tortillas")), 30, 4));
                seedRecipe(new Recipe("mushroom risotto", "Creamy rice with mushrooms",
                                List.of(ingredient("300", G, "arborio rice"), ingredient("250", G, "mushrooms"),
                                                ingredient("50", G, "parmesan"), ingredient("1", PIECE, "onion"),
                                                ingredient("1", L, "vegetable broth")), 40, 4));
                seedRecipe(new Recipe("overnight oats", "Easy make-ahead breakfast",
                                List.of(ingredient("50", G, "oats"), ingredient("1.5", DL, "milk"),
                                                ingredient("1", TBSP, "chia seeds"), ingredient("1", TSP, "maple syrup"),
                                                ingredient("80", G, "berries")), 5, 1));
                seedRecipe(new Recipe("turkey chili", "Hearty chili with beans",
                                List.of(ingredient("500", G, "ground turkey"), ingredient("1", CAN, "kidney beans"),
                                                ingredient("1", CAN, "tomatoes"), ingredient("1", PIECE, "onion"),
                                                ingredient("2", TSP, "chili powder")), 45, 4));
                seedRecipe(new Recipe("caprese skewers", "Fresh tomato mozzarella bites",
                                List.of(ingredient("250", G, "cherry tomatoes"), ingredient("200", G, "mozzarella"),
                                                ingredient("1", BUNCH, "basil"), ingredient("2", TBSP, "balsamic glaze")), 10, 4));
                seedRecipe(new Recipe("egg fried rice", "Quick rice with eggs and veggies",
                                List.of(ingredient("300", G, "rice"), ingredient("3", PIECE, "eggs"),
                                                ingredient("100", G, "peas"), ingredient("1", PIECE, "carrot"),
                                                ingredient("2", TBSP, "soy sauce"), ingredient("2", PIECE, "green onion")), 15, 3));
                seedRecipe(new Recipe("spinach lasagna", "Layered pasta with spinach and ricotta",
                                List.of(ingredient("250", G, "lasagna noodles"), ingredient("500", G, "spinach"),
                                                ingredient("250", G, "ricotta"), ingredient("7", DL, "marinara"),
                                                ingredient("200", G, "mozzarella")), 60, 6));
        }
}
