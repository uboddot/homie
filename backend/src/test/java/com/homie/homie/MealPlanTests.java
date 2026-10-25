package com.homie.homie;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.homie.nutrition.NutritionSource;

import com.jayway.jsonpath.JsonPath;

// every test uses its own dates, because they share one database (and the seeded meals are around today)
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:mealplantestdb")
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class MealPlanTests {

    private static final String RECIPE = """
            {"name":"toast","description":"Quick snack","preparationTime":5,"portions":1,
             "ingredients":[{"amount":2,"unit":"SLICE","name":"bread"}],"steps":[],"shared":false}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean // never ask Gemini in tests
    private NutritionSource nutritionSource;

    private Cookie login(String username, String password) throws Exception {
        return mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getCookie("access_token");
    }

    private long createRecipe(Cookie owner) throws Exception {
        String body = mockMvc.perform(post("/recipes").cookie(owner).contentType(MediaType.APPLICATION_JSON).content(RECIPE))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private static String setMeal(String dates, String slot, Object recipeId) {
        return "{\"dates\":[" + dates + "],\"slot\":\"" + slot + "\",\"recipeId\":" + recipeId + "}";
    }

    @Test
    void plansARecipeOnSeveralDays() throws Exception {
        Cookie anna = login("anna", "password");
        long id = createRecipe(anna);

        mockMvc.perform(put("/meal-plan").cookie(anna).contentType(MediaType.APPLICATION_JSON)
                .content(setMeal("\"2030-01-02\",\"2030-01-01\"", "DINNER", id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].date").value("2030-01-01"))
                .andExpect(jsonPath("$[0].slot").value("DINNER"))
                .andExpect(jsonPath("$[0].recipeId").value(id))
                .andExpect(jsonPath("$[0].recipeName").value("toast"))
                .andExpect(jsonPath("$[1].date").value("2030-01-02"));

        mockMvc.perform(get("/meal-plan?from=2030-01-01&to=2030-01-31").cookie(anna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void replacesAndClearsASlot() throws Exception {
        Cookie anna = login("anna", "password");
        long first = createRecipe(anna);
        long second = createRecipe(anna);

        mockMvc.perform(put("/meal-plan").cookie(anna).contentType(MediaType.APPLICATION_JSON)
                .content(setMeal("\"2030-02-01\",\"2030-02-02\"", "LUNCH", first)))
                .andExpect(status().isOk());
        mockMvc.perform(put("/meal-plan").cookie(anna).contentType(MediaType.APPLICATION_JSON)
                .content(setMeal("\"2030-02-01\"", "LUNCH", second)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].recipeId").value(second));
        mockMvc.perform(put("/meal-plan").cookie(anna).contentType(MediaType.APPLICATION_JSON)
                .content(setMeal("\"2030-02-02\"", "LUNCH", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/meal-plan?from=2030-02-01&to=2030-02-28").cookie(anna))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].date").value("2030-02-01"))
                .andExpect(jsonPath("$[0].recipeId").value(second));
    }

    @Test
    void usersOnlySeeTheirOwnPlan() throws Exception {
        Cookie anna = login("anna", "password");
        long id = createRecipe(anna);
        mockMvc.perform(put("/meal-plan").cookie(anna).contentType(MediaType.APPLICATION_JSON)
                .content(setMeal("\"2030-03-01\"", "BREAKFAST", id)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/meal-plan?from=2030-03-01&to=2030-03-31").cookie(login("admin", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void someoneElsesPrivateRecipeIsForbidden() throws Exception {
        String recipes = mockMvc.perform(get("/recipes").cookie(login("user", "password")))
                .andReturn().getResponse().getContentAsString();
        List<Number> ids = JsonPath.read(recipes, "$[?(@.name == 'shakshuka')].id");

        mockMvc.perform(put("/meal-plan").cookie(login("anna", "password")).contentType(MediaType.APPLICATION_JSON)
                .content(setMeal("\"2030-04-01\"", "DINNER", ids.get(0))))
                .andExpect(status().isForbidden());
    }

    @Test
    void unknownRecipeIsNotFound() throws Exception {
        mockMvc.perform(put("/meal-plan").cookie(login("anna", "password")).contentType(MediaType.APPLICATION_JSON)
                .content(setMeal("\"2030-05-01\"", "DINNER", 999999)))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidRequestsAreRejected() throws Exception {
        Cookie anna = login("anna", "password");

        mockMvc.perform(get("/meal-plan?from=2030-06-02&to=2030-06-01").cookie(anna))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/meal-plan?from=2030-01-01&to=2031-12-31").cookie(anna))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/meal-plan").cookie(anna).contentType(MediaType.APPLICATION_JSON)
                .content(setMeal("", "DINNER", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Pick at least one day"));
        mockMvc.perform(put("/meal-plan").cookie(anna).contentType(MediaType.APPLICATION_JSON)
                .content("{\"dates\":[\"2030-06-01\"],\"recipeId\":null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void notLoggedInIsRejected() throws Exception {
        mockMvc.perform(get("/meal-plan?from=2030-01-01&to=2030-01-31"))
                .andExpect(status().isUnauthorized());
    }
}
