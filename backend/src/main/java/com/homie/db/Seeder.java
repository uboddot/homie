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
                recipeRepository.save(new Recipe("apple pie", "Delicious apple pie",
                                List.of("apples", "flour", "sugar", "butter"), 60));
                recipeRepository.save(new Recipe("spaghetti bolognese", "Classic Italian pasta dish",
                                List.of("spaghetti", "ground beef", "tomato sauce", "onion", "garlic"), 45));
                recipeRepository.save(new Recipe("chocolate chip cookies", "Crispy on the outside, chewy on the inside",
                                List.of("flour", "sugar", "butter", "eggs", "chocolate chips"), 30));
                recipeRepository.save(new Recipe("caesar salad", "Fresh and crunchy salad with creamy dressing",
                                List.of("romaine lettuce", "croutons", "parmesan cheese", "caesar dressing"), 15));
                recipeRepository.save(new Recipe("chicken curry", "Spicy and flavorful chicken curry",
                                List.of("chicken", "curry powder", "coconut milk", "onion", "garlic", "ginger"), 50));
                recipeRepository.save(new Recipe("vegetable stir fry", "Colorful veggies with savory sauce",
                                List.of("broccoli", "bell pepper", "carrot", "soy sauce", "garlic", "ginger"), 20));
                recipeRepository.save(new Recipe("beef tacos", "Crispy tacos with seasoned beef",
                                List.of("taco shells", "ground beef", "lettuce", "tomato", "cheddar", "taco seasoning"),
                                25));
                recipeRepository.save(new Recipe("margherita pizza", "Classic pizza with basil and mozzarella",
                                List.of("pizza dough", "tomato sauce", "mozzarella", "basil", "olive oil"), 30));
                recipeRepository.save(new Recipe("pancakes", "Fluffy breakfast pancakes",
                                List.of("flour", "milk", "eggs", "baking powder", "sugar", "butter"), 15));
                recipeRepository.save(new Recipe("avocado toast", "Simple toast with smashed avocado",
                                List.of("bread", "avocado", "lemon", "salt", "pepper", "chili flakes"), 10));
                recipeRepository.save(new Recipe("greek salad", "Fresh salad with feta and olives",
                                List.of("cucumber", "tomato", "feta", "olives", "red onion", "oregano"), 12));
                recipeRepository.save(new Recipe("tomato basil soup", "Comforting soup with herbs",
                                List.of("tomatoes", "onion", "garlic", "basil", "vegetable broth"), 35));
                recipeRepository.save(new Recipe("grilled cheese", "Golden toast with melted cheese",
                                List.of("bread", "cheddar", "butter"), 10));
                recipeRepository.save(new Recipe("lemon garlic salmon", "Bright and flaky baked salmon",
                                List.of("salmon", "lemon", "garlic", "olive oil", "dill"), 25));
                recipeRepository.save(new Recipe("banana smoothie", "Creamy smoothie with banana",
                                List.of("banana", "milk", "yogurt", "honey", "ice"), 5));
                recipeRepository.save(new Recipe("quinoa bowl", "Healthy bowl with roasted veggies",
                                List.of("quinoa", "zucchini", "chickpeas", "olive oil", "lemon", "spinach"), 30));
                recipeRepository.save(new Recipe("shrimp scampi", "Garlic butter shrimp over pasta",
                                List.of("shrimp", "garlic", "butter", "parsley", "lemon", "linguine"), 20));
                recipeRepository.save(new Recipe("bbq pulled pork", "Slow-cooked pork with smoky sauce",
                                List.of("pork shoulder", "bbq sauce", "onion", "brown sugar", "paprika"), 240));
                recipeRepository.save(new Recipe("chicken fajitas", "Sizzling chicken with peppers",
                                List.of("chicken", "bell peppers", "onion", "fajita seasoning", "tortillas"), 30));
                recipeRepository.save(new Recipe("mushroom risotto", "Creamy rice with mushrooms",
                                List.of("arborio rice", "mushrooms", "parmesan", "onion", "vegetable broth"), 40));
                recipeRepository.save(new Recipe("overnight oats", "Easy make-ahead breakfast",
                                List.of("oats", "milk", "chia seeds", "maple syrup", "berries"), 5));
                recipeRepository.save(new Recipe("turkey chili", "Hearty chili with beans",
                                List.of("ground turkey", "kidney beans", "tomatoes", "onion", "chili powder"), 45));
                recipeRepository.save(new Recipe("caprese skewers", "Fresh tomato mozzarella bites",
                                List.of("cherry tomatoes", "mozzarella", "basil", "balsamic glaze"), 10));
                recipeRepository.save(new Recipe("egg fried rice", "Quick rice with eggs and veggies",
                                List.of("rice", "eggs", "peas", "carrot", "soy sauce", "green onion"), 15));
                recipeRepository.save(new Recipe("spinach lasagna", "Layered pasta with spinach and ricotta",
                                List.of("lasagna noodles", "spinach", "ricotta", "marinara", "mozzarella"), 60));
        }
}
