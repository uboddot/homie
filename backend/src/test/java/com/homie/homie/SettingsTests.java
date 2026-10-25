package com.homie.homie;

import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.test.web.servlet.ResultActions;

import com.homie.nutrition.NutritionSource;

// the tests share one database: admin is never changed, user gets targets, anna toggles AI
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:settingstestdb")
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class SettingsTests {

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

    private ResultActions saveSettings(Cookie cookie, String body) throws Exception {
        return mockMvc.perform(put("/settings").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private void createRecipe(Cookie cookie, String ingredient) throws Exception {
        mockMvc.perform(post("/recipes").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"test","description":"","preparationTime":5,"portions":1,
                 "ingredients":[{"amount":100,"unit":"G","name":"%s"}],"steps":[],"shared":false}
                """.formatted(ingredient)))
                .andExpect(status().isCreated());
    }

    @Test
    void requiresLogin() throws Exception {
        mockMvc.perform(get("/settings")).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/settings").contentType(MediaType.APPLICATION_JSON).content("{\"useAi\":true}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returnsDefaultsWhenNothingWasSaved() throws Exception {
        mockMvc.perform(get("/settings").cookie(login("admin", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.useAi").value(true))
                .andExpect(jsonPath("$.protein").doesNotExist())
                .andExpect(jsonPath("$.calories").doesNotExist());
    }

    @Test
    void savesAGoalAndARangePerUser() throws Exception {
        Cookie user = login("user", "password");
        saveSettings(user, """
                {"useAi":true,"protein":{"goal":40},"calories":{"min":500,"max":800}}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.protein.goal").value(40));

        mockMvc.perform(get("/settings").cookie(user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.protein.goal").value(40))
                .andExpect(jsonPath("$.protein.min").doesNotExist())
                .andExpect(jsonPath("$.calories.goal").doesNotExist())
                .andExpect(jsonPath("$.calories.min").value(500))
                .andExpect(jsonPath("$.calories.max").value(800));

        // only one side of a range, and clearing a target
        saveSettings(user, """
                {"useAi":true,"protein":{"min":30},"calories":null}
                """).andExpect(status().isOk());
        mockMvc.perform(get("/settings").cookie(user))
                .andExpect(jsonPath("$.protein.min").value(30))
                .andExpect(jsonPath("$.protein.max").doesNotExist())
                .andExpect(jsonPath("$.calories").doesNotExist());

        mockMvc.perform(get("/settings").cookie(login("admin", "password123")))
                .andExpect(jsonPath("$.protein").doesNotExist());
    }

    @Test
    void rejectsInvalidTargets() throws Exception {
        Cookie user = login("user", "password");
        saveSettings(user, "{\"useAi\":true,\"protein\":{\"goal\":40,\"min\":30}}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Choose either a goal or a range"));
        saveSettings(user, "{\"useAi\":true,\"calories\":{\"min\":0}}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Targets must be positive"));
        saveSettings(user, "{\"useAi\":true,\"calories\":{\"min\":900,\"max\":500}}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The minimum can't be above the maximum"));
    }

    @Test
    void geminiIsOnlyAskedWhenAiIsOn() throws Exception {
        Cookie anna = login("anna", "password");

        saveSettings(anna, "{\"useAi\":false}").andExpect(status().isOk());
        createRecipe(anna, "dragon fruit");
        verify(nutritionSource, never()).fetch(anySet(), anySet());

        saveSettings(anna, "{\"useAi\":true}").andExpect(status().isOk());
        createRecipe(anna, "star fruit");
        verify(nutritionSource, times(1)).fetch(anySet(), anySet());
    }
}
