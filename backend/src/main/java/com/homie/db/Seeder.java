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
import java.util.List;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.homie.model.AppUser;
import com.homie.model.Ingredient;
import com.homie.model.IngredientUnit;
import com.homie.model.Recipe;
import com.homie.model.Step;
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

        private AppUser anna;
        private AppUser admin;
        private AppUser user;
        private int seededRecipes = 0;

        public Seeder(RecipeRepository recipeRepository, StepRepository stepRepository,
                        UserRepository userRepository, PasswordEncoder passwordEncoder) {
                this.recipeRepository = recipeRepository;
                this.stepRepository = stepRepository;
                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
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

        /** Private recipe of the given owner, steps are numbered in the given order. */
        private void seedOwnRecipe(Recipe recipe, AppUser owner, String... steps) {
                recipe.setOwner(owner);
                recipe.setShared(false);
                recipeRepository.save(recipe);
                for (int i = 0; i < steps.length; i++) {
                        stepRepository.save(new Step(recipe, i + 1, steps[i]));
                }
        }

        private static Ingredient ingredient(String amount, IngredientUnit unit, String name) {
                return new Ingredient(new BigDecimal(amount), unit, name);
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

                // user's own private recipes, with steps
                seedOwnRecipe(new Recipe("apple pie", "Delicious homemade apple pie",
                                List.of(ingredient("4", PIECE, "apples"), ingredient("250", G, "flour"),
                                                ingredient("100", G, "sugar"), ingredient("125", G, "butter"),
                                                ingredient("1", TSP, "cinnamon")), 60), user,
                                "Preheat oven to 375°F (190°C).",
                                "Rub butter into flour and a pinch of sugar, add cold water and form a dough.",
                                "Peel and slice the apples, toss with sugar and cinnamon.",
                                "Line a pie dish with half the dough, add the apples and cover with the rest.",
                                "Bake for 45 minutes until golden brown.");
                seedOwnRecipe(new Recipe("pasta aglio e olio", "Simple spaghetti with garlic and olive oil",
                                List.of(ingredient("200", G, "spaghetti"), ingredient("3", CLOVE, "garlic"),
                                                ingredient("4", TBSP, "olive oil"), ingredient("1", PINCH, "chili flakes"),
                                                ingredient("1", BUNCH, "parsley")), 20), user,
                                "Cook the spaghetti in salted water until al dente.",
                                "Gently fry sliced garlic and chili flakes in olive oil until lightly golden.",
                                "Toss the drained spaghetti in the oil with a splash of pasta water.",
                                "Finish with chopped parsley and serve immediately.");
                seedOwnRecipe(new Recipe("shakshuka", "Eggs poached in a spiced tomato sauce",
                                List.of(ingredient("4", PIECE, "eggs"), ingredient("1", CAN, "tomatoes"),
                                                ingredient("1", PIECE, "bell pepper"), ingredient("1", PIECE, "onion"),
                                                ingredient("1", TSP, "cumin"), ingredient("1", TSP, "paprika")), 30), user,
                                "Sauté diced onion and bell pepper in a pan until soft.",
                                "Add cumin and paprika, then the chopped tomatoes, and simmer for 10 minutes.",
                                "Make small wells in the sauce and crack an egg into each.",
                                "Cover and cook until the egg whites are set, about 6 minutes.",
                                "Serve hot with bread.");

                // Seed a sample recipe when the application context starts
                seedRecipe(new Recipe("spaghetti bolognese", "Classic Italian pasta dish",
                                List.of(ingredient("400", G, "spaghetti"), ingredient("500", G, "ground beef"),
                                                ingredient("500", ML, "tomato sauce"), ingredient("1", PIECE, "onion"),
                                                ingredient("2", CLOVE, "garlic")), 45));
                seedRecipe(new Recipe("chocolate chip cookies", "Crispy on the outside, chewy on the inside",
                                List.of(ingredient("280", G, "flour"), ingredient("200", G, "sugar"),
                                                ingredient("170", G, "butter"), ingredient("2", PIECE, "eggs"),
                                                ingredient("200", G, "chocolate chips")), 30));
                seedRecipe(new Recipe("caesar salad", "Fresh and crunchy salad with creamy dressing",
                                List.of(ingredient("1", PIECE, "romaine lettuce"), ingredient("100", G, "croutons"),
                                                ingredient("50", G, "parmesan cheese"), ingredient("4", TBSP, "caesar dressing")), 15));
                seedRecipe(new Recipe("chicken curry", "Spicy and flavorful chicken curry",
                                List.of(ingredient("600", G, "chicken"), ingredient("2", TBSP, "curry powder"),
                                                ingredient("400", ML, "coconut milk"), ingredient("1", PIECE, "onion"),
                                                ingredient("3", CLOVE, "garlic"), ingredient("1", TBSP, "ginger")), 50));
                seedRecipe(new Recipe("vegetable stir fry", "Colorful veggies with savory sauce",
                                List.of(ingredient("1", PIECE, "broccoli"), ingredient("1", PIECE, "bell pepper"),
                                                ingredient("2", PIECE, "carrot"), ingredient("3", TBSP, "soy sauce"),
                                                ingredient("2", CLOVE, "garlic"), ingredient("1", TSP, "ginger")), 20));
                seedRecipe(new Recipe("beef tacos", "Crispy tacos with seasoned beef",
                                List.of(ingredient("8", PIECE, "taco shells"), ingredient("500", G, "ground beef"),
                                                ingredient("0.5", PIECE, "lettuce"), ingredient("2", PIECE, "tomato"),
                                                ingredient("100", G, "cheddar"), ingredient("2", TBSP, "taco seasoning")), 25));
                seedRecipe(new Recipe("margherita pizza", "Classic pizza with basil and mozzarella",
                                List.of(ingredient("400", G, "pizza dough"), ingredient("150", ML, "tomato sauce"),
                                                ingredient("125", G, "mozzarella"), ingredient("1", BUNCH, "basil"),
                                                ingredient("1", TBSP, "olive oil")), 30));
                seedRecipe(new Recipe("pancakes", "Fluffy breakfast pancakes",
                                List.of(ingredient("200", G, "flour"), ingredient("3", DL, "milk"),
                                                ingredient("2", PIECE, "eggs"), ingredient("2", TSP, "baking powder"),
                                                ingredient("1", TBSP, "sugar"), ingredient("30", G, "butter")), 15));
                seedRecipe(new Recipe("avocado toast", "Simple toast with smashed avocado",
                                List.of(ingredient("2", SLICE, "bread"), ingredient("1", PIECE, "avocado"),
                                                ingredient("0.5", PIECE, "lemon"), ingredient("salt"),
                                                ingredient("pepper"), ingredient("1", PINCH, "chili flakes")), 10));
                seedRecipe(new Recipe("greek salad", "Fresh salad with feta and olives",
                                List.of(ingredient("1", PIECE, "cucumber"), ingredient("3", PIECE, "tomato"),
                                                ingredient("200", G, "feta"), ingredient("100", G, "olives"),
                                                ingredient("0.5", PIECE, "red onion"), ingredient("1", TSP, "oregano")), 12));
                seedRecipe(new Recipe("tomato basil soup", "Comforting soup with herbs",
                                List.of(ingredient("1", KG, "tomatoes"), ingredient("1", PIECE, "onion"),
                                                ingredient("2", CLOVE, "garlic"), ingredient("1", BUNCH, "basil"),
                                                ingredient("5", DL, "vegetable broth")), 35));
                seedRecipe(new Recipe("grilled cheese", "Golden toast with melted cheese",
                                List.of(ingredient("2", SLICE, "bread"), ingredient("2", SLICE, "cheddar"),
                                                ingredient("1", TBSP, "butter")), 10));
                seedRecipe(new Recipe("lemon garlic salmon", "Bright and flaky baked salmon",
                                List.of(ingredient("400", G, "salmon"), ingredient("1", PIECE, "lemon"),
                                                ingredient("2", CLOVE, "garlic"), ingredient("2", TBSP, "olive oil"),
                                                ingredient("1", BUNCH, "dill")), 25));
                seedRecipe(new Recipe("banana smoothie", "Creamy smoothie with banana",
                                List.of(ingredient("2", PIECE, "banana"), ingredient("2", DL, "milk"),
                                                ingredient("150", G, "yogurt"), ingredient("1", TBSP, "honey"),
                                                ingredient("ice")), 5));
                seedRecipe(new Recipe("quinoa bowl", "Healthy bowl with roasted veggies",
                                List.of(ingredient("150", G, "quinoa"), ingredient("1", PIECE, "zucchini"),
                                                ingredient("1", CAN, "chickpeas"), ingredient("2", TBSP, "olive oil"),
                                                ingredient("0.5", PIECE, "lemon"), ingredient("100", G, "spinach")), 30));
                seedRecipe(new Recipe("shrimp scampi", "Garlic butter shrimp over pasta",
                                List.of(ingredient("400", G, "shrimp"), ingredient("4", CLOVE, "garlic"),
                                                ingredient("50", G, "butter"), ingredient("1", BUNCH, "parsley"),
                                                ingredient("1", PIECE, "lemon"), ingredient("300", G, "linguine")), 20));
                seedRecipe(new Recipe("bbq pulled pork", "Slow-cooked pork with smoky sauce",
                                List.of(ingredient("2", KG, "pork shoulder"), ingredient("3", DL, "bbq sauce"),
                                                ingredient("1", PIECE, "onion"), ingredient("2", TBSP, "brown sugar"),
                                                ingredient("1", TBSP, "paprika")), 240));
                seedRecipe(new Recipe("chicken fajitas", "Sizzling chicken with peppers",
                                List.of(ingredient("500", G, "chicken"), ingredient("2", PIECE, "bell peppers"),
                                                ingredient("1", PIECE, "onion"), ingredient("2", TBSP, "fajita seasoning"),
                                                ingredient("8", PIECE, "tortillas")), 30));
                seedRecipe(new Recipe("mushroom risotto", "Creamy rice with mushrooms",
                                List.of(ingredient("300", G, "arborio rice"), ingredient("250", G, "mushrooms"),
                                                ingredient("50", G, "parmesan"), ingredient("1", PIECE, "onion"),
                                                ingredient("1", L, "vegetable broth")), 40));
                seedRecipe(new Recipe("overnight oats", "Easy make-ahead breakfast",
                                List.of(ingredient("50", G, "oats"), ingredient("1.5", DL, "milk"),
                                                ingredient("1", TBSP, "chia seeds"), ingredient("1", TSP, "maple syrup"),
                                                ingredient("80", G, "berries")), 5));
                seedRecipe(new Recipe("turkey chili", "Hearty chili with beans",
                                List.of(ingredient("500", G, "ground turkey"), ingredient("1", CAN, "kidney beans"),
                                                ingredient("1", CAN, "tomatoes"), ingredient("1", PIECE, "onion"),
                                                ingredient("2", TSP, "chili powder")), 45));
                seedRecipe(new Recipe("caprese skewers", "Fresh tomato mozzarella bites",
                                List.of(ingredient("250", G, "cherry tomatoes"), ingredient("200", G, "mozzarella"),
                                                ingredient("1", BUNCH, "basil"), ingredient("2", TBSP, "balsamic glaze")), 10));
                seedRecipe(new Recipe("egg fried rice", "Quick rice with eggs and veggies",
                                List.of(ingredient("300", G, "rice"), ingredient("3", PIECE, "eggs"),
                                                ingredient("100", G, "peas"), ingredient("1", PIECE, "carrot"),
                                                ingredient("2", TBSP, "soy sauce"), ingredient("2", PIECE, "green onion")), 15));
                seedRecipe(new Recipe("spinach lasagna", "Layered pasta with spinach and ricotta",
                                List.of(ingredient("250", G, "lasagna noodles"), ingredient("500", G, "spinach"),
                                                ingredient("250", G, "ricotta"), ingredient("7", DL, "marinara"),
                                                ingredient("200", G, "mozzarella")), 60));
        }
}
