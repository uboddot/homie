package com.homie.db;

import java.util.List;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.homie.model.AppUser;
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

        @PostConstruct
        public void seed() {
                // Dev users
                anna = userRepository.save(new AppUser("anna", "anna@homie.local",
                                passwordEncoder.encode("password"), ApplicationUserRole.USER));
                admin = userRepository.save(new AppUser("admin", "admin@homie.local",
                                passwordEncoder.encode("password123"), ApplicationUserRole.ADMIN));
                user = userRepository.save(new AppUser("user", "user@homie.local",
                                passwordEncoder.encode("password"), ApplicationUserRole.USER));

                // user's own private recipes, with steps
                seedOwnRecipe(new Recipe("apple pie", "Delicious homemade apple pie",
                                List.of("apples", "flour", "sugar", "butter", "cinnamon"), 60), user,
                                "Preheat oven to 375°F (190°C).",
                                "Rub butter into flour and a pinch of sugar, add cold water and form a dough.",
                                "Peel and slice the apples, toss with sugar and cinnamon.",
                                "Line a pie dish with half the dough, add the apples and cover with the rest.",
                                "Bake for 45 minutes until golden brown.");
                seedOwnRecipe(new Recipe("pasta aglio e olio", "Simple spaghetti with garlic and olive oil",
                                List.of("spaghetti", "garlic", "olive oil", "chili flakes", "parsley"), 20), user,
                                "Cook the spaghetti in salted water until al dente.",
                                "Gently fry sliced garlic and chili flakes in olive oil until lightly golden.",
                                "Toss the drained spaghetti in the oil with a splash of pasta water.",
                                "Finish with chopped parsley and serve immediately.");
                seedOwnRecipe(new Recipe("shakshuka", "Eggs poached in a spiced tomato sauce",
                                List.of("eggs", "tomatoes", "bell pepper", "onion", "cumin", "paprika"), 30), user,
                                "Sauté diced onion and bell pepper in a pan until soft.",
                                "Add cumin and paprika, then the chopped tomatoes, and simmer for 10 minutes.",
                                "Make small wells in the sauce and crack an egg into each.",
                                "Cover and cook until the egg whites are set, about 6 minutes.",
                                "Serve hot with bread.");

                // Seed a sample recipe when the application context starts
                seedRecipe(new Recipe("spaghetti bolognese", "Classic Italian pasta dish",
                                List.of("spaghetti", "ground beef", "tomato sauce", "onion", "garlic"), 45));
                seedRecipe(new Recipe("chocolate chip cookies", "Crispy on the outside, chewy on the inside",
                                List.of("flour", "sugar", "butter", "eggs", "chocolate chips"), 30));
                seedRecipe(new Recipe("caesar salad", "Fresh and crunchy salad with creamy dressing",
                                List.of("romaine lettuce", "croutons", "parmesan cheese", "caesar dressing"), 15));
                seedRecipe(new Recipe("chicken curry", "Spicy and flavorful chicken curry",
                                List.of("chicken", "curry powder", "coconut milk", "onion", "garlic", "ginger"), 50));
                seedRecipe(new Recipe("vegetable stir fry", "Colorful veggies with savory sauce",
                                List.of("broccoli", "bell pepper", "carrot", "soy sauce", "garlic", "ginger"), 20));
                seedRecipe(new Recipe("beef tacos", "Crispy tacos with seasoned beef",
                                List.of("taco shells", "ground beef", "lettuce", "tomato", "cheddar", "taco seasoning"),
                                25));
                seedRecipe(new Recipe("margherita pizza", "Classic pizza with basil and mozzarella",
                                List.of("pizza dough", "tomato sauce", "mozzarella", "basil", "olive oil"), 30));
                seedRecipe(new Recipe("pancakes", "Fluffy breakfast pancakes",
                                List.of("flour", "milk", "eggs", "baking powder", "sugar", "butter"), 15));
                seedRecipe(new Recipe("avocado toast", "Simple toast with smashed avocado",
                                List.of("bread", "avocado", "lemon", "salt", "pepper", "chili flakes"), 10));
                seedRecipe(new Recipe("greek salad", "Fresh salad with feta and olives",
                                List.of("cucumber", "tomato", "feta", "olives", "red onion", "oregano"), 12));
                seedRecipe(new Recipe("tomato basil soup", "Comforting soup with herbs",
                                List.of("tomatoes", "onion", "garlic", "basil", "vegetable broth"), 35));
                seedRecipe(new Recipe("grilled cheese", "Golden toast with melted cheese",
                                List.of("bread", "cheddar", "butter"), 10));
                seedRecipe(new Recipe("lemon garlic salmon", "Bright and flaky baked salmon",
                                List.of("salmon", "lemon", "garlic", "olive oil", "dill"), 25));
                seedRecipe(new Recipe("banana smoothie", "Creamy smoothie with banana",
                                List.of("banana", "milk", "yogurt", "honey", "ice"), 5));
                seedRecipe(new Recipe("quinoa bowl", "Healthy bowl with roasted veggies",
                                List.of("quinoa", "zucchini", "chickpeas", "olive oil", "lemon", "spinach"), 30));
                seedRecipe(new Recipe("shrimp scampi", "Garlic butter shrimp over pasta",
                                List.of("shrimp", "garlic", "butter", "parsley", "lemon", "linguine"), 20));
                seedRecipe(new Recipe("bbq pulled pork", "Slow-cooked pork with smoky sauce",
                                List.of("pork shoulder", "bbq sauce", "onion", "brown sugar", "paprika"), 240));
                seedRecipe(new Recipe("chicken fajitas", "Sizzling chicken with peppers",
                                List.of("chicken", "bell peppers", "onion", "fajita seasoning", "tortillas"), 30));
                seedRecipe(new Recipe("mushroom risotto", "Creamy rice with mushrooms",
                                List.of("arborio rice", "mushrooms", "parmesan", "onion", "vegetable broth"), 40));
                seedRecipe(new Recipe("overnight oats", "Easy make-ahead breakfast",
                                List.of("oats", "milk", "chia seeds", "maple syrup", "berries"), 5));
                seedRecipe(new Recipe("turkey chili", "Hearty chili with beans",
                                List.of("ground turkey", "kidney beans", "tomatoes", "onion", "chili powder"), 45));
                seedRecipe(new Recipe("caprese skewers", "Fresh tomato mozzarella bites",
                                List.of("cherry tomatoes", "mozzarella", "basil", "balsamic glaze"), 10));
                seedRecipe(new Recipe("egg fried rice", "Quick rice with eggs and veggies",
                                List.of("rice", "eggs", "peas", "carrot", "soy sauce", "green onion"), 15));
                seedRecipe(new Recipe("spinach lasagna", "Layered pasta with spinach and ricotta",
                                List.of("lasagna noodles", "spinach", "ricotta", "marinara", "mozzarella"), 60));
        }
}
