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
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:recipetestdb")
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class RecipeCreationTests {

    private static final String RECIPE = """
            {"name":" toast ","description":"Quick snack","preparationTime":5,
             "ingredients":["bread","","butter"],"steps":["Toast the bread.","  ","Spread the butter."],
             "shared":false}
            """;

    @Autowired
    private MockMvc mockMvc;

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
                .andExpect(jsonPath("$.ingredients.length()").value(2))
                .andExpect(jsonPath("$.steps.length()").value(2))
                .andExpect(jsonPath("$.steps[0].stepOrder").value(1))
                .andExpect(jsonPath("$.steps[1].instruction").value("Spread the butter."));
    }

    @Test
    void nameIsRequired() throws Exception {
        mockMvc.perform(post("/recipes").cookie(loginAsAnna()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\" \",\"ingredients\":[\"bread\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Name is required"));
    }

    @Test
    void notLoggedInIsRejected() throws Exception {
        mockMvc.perform(post("/recipes").contentType(MediaType.APPLICATION_JSON).content(RECIPE))
                .andExpect(status().isUnauthorized());
    }
}
