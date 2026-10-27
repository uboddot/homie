package com.homie.homie;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.homie.jwt.JwtService;
import com.homie.nutrition.NutritionSource;
import com.homie.security.ApplicationUserRole;

@SpringBootTest(properties = "spring.datasource.url=jdbc:tc:postgresql:17-alpine:///recipetestdb")
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class RecipeCreationTests {

    private static final String RECIPE = """
            {"name":" toast ","description":"Quick snack","preparationTime":5,"portions":2,
             "ingredients":[{"amount":2,"unit":"SLICE","name":" bread "},{"name":""},{"amount":0.5,"unit":"TSP","name":"butter"}],"steps":["Toast the bread.","  ","Spread the butter."],
             "shared":false}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean // never ask Gemini in tests
    private NutritionSource nutritionSource;

    private Cookie loginAsAnna() throws Exception {
        return mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"anna\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getCookie("access_token");
    }

    @Test
    void createdRecipeBelongsToTheLoggedInUser() throws Exception {
        mockMvc.perform(post("/recipes").cookie(loginAsAnna()).contentType(MediaType.APPLICATION_JSON).content(RECIPE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("toast"))
                .andExpect(jsonPath("$.ownerUsername").value("anna"))
                .andExpect(jsonPath("$.portions").value(2))
                .andExpect(jsonPath("$.ingredients.length()").value(2))
                .andExpect(jsonPath("$.ingredients[0].name").value("bread"))
                .andExpect(jsonPath("$.ingredients[0].unit").value("SLICE"))
                .andExpect(jsonPath("$.ingredients[1].amount").value(0.5))
                .andExpect(jsonPath("$.ingredients[1].unit").value("TSP"))
                .andExpect(jsonPath("$.steps.length()").value(2))
                .andExpect(jsonPath("$.steps[0].stepOrder").value(1))
                .andExpect(jsonPath("$.steps[1].instruction").value("Spread the butter."));
    }

    @Test
    void nameIsRequired() throws Exception {
        mockMvc.perform(post("/recipes").cookie(loginAsAnna()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\" \",\"ingredients\":[{\"name\":\"bread\"}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Name is required"));
    }

    @Test
    void portionsDefaultToFour() throws Exception {
        mockMvc.perform(post("/recipes").cookie(loginAsAnna()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"toast\",\"ingredients\":[{\"name\":\"bread\"}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.portions").value(4));
    }

    @Test
    void portionsMustBePositive() throws Exception {
        mockMvc.perform(post("/recipes").cookie(loginAsAnna()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"toast\",\"portions\":0,\"ingredients\":[{\"name\":\"bread\"}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Portions must be at least 1"));
    }

    @Test
    void unitNeedsAnAmount() throws Exception {
        expectBadRequest("[{\"unit\":\"G\",\"name\":\"flour\"}]", "A unit needs an amount");
    }

    @Test
    void amountNeedsAnIngredient() throws Exception {
        expectBadRequest("[{\"amount\":200,\"unit\":\"G\",\"name\":\" \"}]", "Every amount needs an ingredient");
    }

    @Test
    void amountMustBePositive() throws Exception {
        expectBadRequest("[{\"amount\":-1,\"name\":\"flour\"}]", "Amounts must be positive");
    }

    @Test
    void unknownUnitIsRejected() throws Exception {
        mockMvc.perform(post("/recipes").cookie(loginAsAnna()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"toast\",\"ingredients\":[{\"amount\":1,\"unit\":\"BUCKET\",\"name\":\"water\"}]}"))
                .andExpect(status().isBadRequest());
    }

    private void expectBadRequest(String ingredients, String message) throws Exception {
        mockMvc.perform(post("/recipes").cookie(loginAsAnna()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"toast\",\"ingredients\":" + ingredients + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(message));
    }

    @Test
    void notLoggedInIsRejected() throws Exception {
        mockMvc.perform(post("/recipes").contentType(MediaType.APPLICATION_JSON).content(RECIPE))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validTokenOfAUserThatNoLongerExistsIsRejected() throws Exception {
        Cookie ghost = new Cookie("access_token",
                jwtService.createToken("ghost", ApplicationUserRole.USER.getGrantedAuthorities()));
        mockMvc.perform(post("/recipes").cookie(ghost).contentType(MediaType.APPLICATION_JSON).content(RECIPE))
                .andExpect(status().isUnauthorized());
    }
}
