package com.homie.homie;

import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.homie.model.IngredientUnit;
import com.homie.nutrition.IngredientNutrition;
import com.homie.nutrition.IngredientUnitWeight;
import com.homie.nutrition.NutritionSource;
import com.homie.nutrition.NutritionSource.Fetched;
import com.homie.nutrition.NutritionSource.UnitKey;
import com.jayway.jsonpath.JsonPath;

/**
 * Nutrition suggestions with a fake source that knows ingredients by their last word, so every test can use its
 * own names ("a potato", "b potato") and start with an empty cache.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:nutritiontestdb")
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class NutritionTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NutritionSource nutritionSource;

    /** potato, rice and oil with all macros, sugar only with carbs; "mystery" is unknown. */
    @BeforeEach
    void fakeSource() {
        when(nutritionSource.fetch(anySet(), anySet())).thenAnswer(invocation -> {
            Set<String> names = invocation.getArgument(0);
            Set<UnitKey> units = invocation.getArgument(1);
            List<IngredientNutrition> ingredients = new ArrayList<>();
            for (String name : names) {
                if (name.endsWith("potato")) {
                    ingredients.add(perGram(name, "0.001", "0.17", "0.02"));
                } else if (name.endsWith("rice")) {
                    ingredients.add(perGram(name, "0.005", "0.8", "0.07"));
                } else if (name.endsWith("oil")) {
                    ingredients.add(perGram(name, "1", "0", "0"));
                } else if (name.endsWith("sugar")) {
                    ingredients.add(perGram(name, null, "1", null));
                }
            }
            List<IngredientUnitWeight> weights = new ArrayList<>();
            for (UnitKey key : units) {
                if (key.name().endsWith("potato") && key.unit() == IngredientUnit.PIECE) {
                    weights.add(new IngredientUnitWeight(key.name(), key.unit(), new BigDecimal("200")));
                }
            }
            return new Fetched(ingredients, weights);
        });
    }

    private static IngredientNutrition perGram(String name, String fat, String carbs, String protein) {
        return new IngredientNutrition(name, fat == null ? null : new BigDecimal(fat),
                carbs == null ? null : new BigDecimal(carbs), protein == null ? null : new BigDecimal(protein));
    }

    private Cookie loginAsAnna() throws Exception {
        return mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"anna\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getCookie("access_token");
    }

    private static String recipe(int portions, String ingredients, String nutrition) {
        return "{\"name\":\"test\",\"portions\":" + portions + ",\"ingredients\":[" + ingredients + "]"
                + (nutrition == null ? "" : ",\"nutrition\":" + nutrition) + "}";
    }

    private ResultActions create(Cookie cookie, String body) throws Exception {
        return mockMvc.perform(post("/recipes").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private ResultActions update(Cookie cookie, long id, String body) throws Exception {
        return mockMvc.perform(put("/recipes/" + id).cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private static long id(ResultActions result) throws Exception {
        return ((Number) JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id")).longValue();
    }

    @Test
    void macrosPerPortionAreSuggestedFromTheIngredients() throws Exception {
        // 2 pieces à 200 g + 0.5 kg + 20 g, for 2 portions; salt without amount is ignored
        create(loginAsAnna(), recipe(2, """
                {"amount":2,"unit":"PIECE","name":"c potato"},{"amount":0.5,"unit":"KG","name":"c rice"},
                {"amount":20,"unit":"G","name":"c oil"},{"name":"salt"}""", null))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nutrition.suggested").value(true))
                // fat: (0.4 + 2.5 + 20) / 2, carbs: (68 + 400) / 2, protein: (8 + 35) / 2
                .andExpect(jsonPath("$.nutrition.fat").value(11.5))
                .andExpect(jsonPath("$.nutrition.carbs").value(234.0))
                .andExpect(jsonPath("$.nutrition.protein").value(21.5))
                // 9 × 11.5 + 4 × 234 + 4 × 21.5
                .andExpect(jsonPath("$.nutrition.calories").value(1126));
    }

    @Test
    void cachedIngredientsAreNotFetchedAgain() throws Exception {
        Cookie anna = loginAsAnna();
        String body = recipe(1, """
                {"amount":1,"unit":"PIECE","name":"d potato"},{"amount":100,"unit":"G","name":"d rice"}""", null);
        create(anna, body).andExpect(status().isCreated());
        create(anna, body.replace("d potato", "  D   Potato ")).andExpect(status().isCreated())
                .andExpect(jsonPath("$.nutrition.carbs").value(114.0));

        verify(nutritionSource, times(1)).fetch(anySet(), anySet());
    }

    @Test
    void unknownIngredientsLeaveTheirMacrosOpen() throws Exception {
        Cookie anna = loginAsAnna();
        create(anna, recipe(1, """
                {"amount":100,"unit":"G","name":"e sugar"}""", null))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nutrition.carbs").value(100.0))
                .andExpect(jsonPath("$.nutrition.fat").isEmpty())
                .andExpect(jsonPath("$.nutrition.protein").isEmpty())
                .andExpect(jsonPath("$.nutrition.calories").isEmpty());

        create(anna, recipe(1, """
                {"amount":1,"unit":"PIECE","name":"mystery"}""", null))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nutrition").isEmpty());
    }

    @Test
    void enteredMacrosAreNotASuggestion() throws Exception {
        create(loginAsAnna(), recipe(1, """
                {"amount":100,"unit":"G","name":"f potato"}""", """
                {"fat":10,"carbs":20,"protein":5}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nutrition.suggested").value(false))
                .andExpect(jsonPath("$.nutrition.fat").value(10.0))
                .andExpect(jsonPath("$.nutrition.calories").value(190));

        verify(nutritionSource, never()).fetch(anySet(), anySet());
    }

    @Test
    void suggestionFollowsTheIngredientsUntilTheUserEditsIt() throws Exception {
        Cookie anna = loginAsAnna();
        long id = id(create(anna, recipe(1, """
                {"amount":100,"unit":"G","name":"g potato"}""", null))
                .andExpect(jsonPath("$.nutrition.carbs").value(17.0)));

        update(anna, id, recipe(1, """
                {"amount":200,"unit":"G","name":"g potato"}""", null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nutrition.suggested").value(true))
                .andExpect(jsonPath("$.nutrition.carbs").value(34.0));

        update(anna, id, recipe(1, """
                {"amount":200,"unit":"G","name":"g potato"}""", """
                {"fat":1,"carbs":2,"protein":3}"""))
                .andExpect(jsonPath("$.nutrition.suggested").value(false));

        // the frontend sends the user's values back, so they survive an ingredient change
        update(anna, id, recipe(1, """
                {"amount":300,"unit":"G","name":"g potato"}""", """
                {"fat":1,"carbs":2,"protein":3}"""))
                .andExpect(jsonPath("$.nutrition.suggested").value(false))
                .andExpect(jsonPath("$.nutrition.carbs").value(2.0))
                .andExpect(jsonPath("$.nutrition.calories").value(29));
    }

    @Test
    void macrosMustBeCompleteAndNotNegative() throws Exception {
        Cookie anna = loginAsAnna();
        create(anna, recipe(1, """
                {"name":"bread"}""", """
                {"fat":1,"carbs":2}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Enter fat, carbs and protein"));
        create(anna, recipe(1, """
                {"name":"bread"}""", """
                {"fat":-1,"carbs":2,"protein":3}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Macros can't be negative"));
    }
}
