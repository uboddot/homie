package com.homie.homie;

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
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:recipeupdatetestdb")
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class RecipeUpdateTests {

    private static final String RECIPE = """
            {"name":"toast","description":"Quick snack","preparationTime":5,
             "ingredients":[{"amount":2,"unit":"SLICE","name":"bread"},{"name":"butter"}],"steps":["Toast the bread.","Spread the butter."],
             "shared":false}
            """;

    private static final String EDITED = """
            {"name":" jam toast ","description":"Sweeter","preparationTime":7,
             "ingredients":[{"amount":1,"unit":"SLICE","name":"bread"},{"name":" "},{"amount":2,"unit":"TBSP","name":" jam "}],"steps":["  ","Spread the jam."],
             "shared":true}
            """;

    @Autowired
    private MockMvc mockMvc;

    private Cookie login(String username, String password) throws Exception {
        return mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getCookie("access_token");
    }

    private long createAsAnna(Cookie anna) throws Exception {
        String body = mockMvc.perform(post("/recipes").cookie(anna).contentType(MediaType.APPLICATION_JSON).content(RECIPE))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    @Test
    void ownerCanEditTheRecipe() throws Exception {
        Cookie anna = login("anna", "password");
        long id = createAsAnna(anna);

        mockMvc.perform(put("/recipes/" + id).cookie(anna).contentType(MediaType.APPLICATION_JSON).content(EDITED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("jam toast"))
                .andExpect(jsonPath("$.preparationTime").value(7))
                .andExpect(jsonPath("$.shared").value(true))
                .andExpect(jsonPath("$.ownerUsername").value("anna"))
                .andExpect(jsonPath("$.ingredients.length()").value(2))
                .andExpect(jsonPath("$.ingredients[1].amount").value(2))
                .andExpect(jsonPath("$.ingredients[1].unit").value("TBSP"))
                .andExpect(jsonPath("$.ingredients[1].name").value("jam"))
                .andExpect(jsonPath("$.steps.length()").value(1))
                .andExpect(jsonPath("$.steps[0].stepOrder").value(1))
                .andExpect(jsonPath("$.steps[0].instruction").value("Spread the jam."));
    }

    @Test
    void othersCantEditTheRecipe() throws Exception {
        long id = createAsAnna(login("anna", "password"));

        mockMvc.perform(put("/recipes/" + id).cookie(login("user", "password"))
                .contentType(MediaType.APPLICATION_JSON).content(EDITED))
                .andExpect(status().isForbidden());
    }

    @Test
    void unknownRecipeIsNotFound() throws Exception {
        mockMvc.perform(put("/recipes/999999").cookie(login("anna", "password"))
                .contentType(MediaType.APPLICATION_JSON).content(EDITED))
                .andExpect(status().isNotFound());
    }

    @Test
    void nameIsRequired() throws Exception {
        Cookie anna = login("anna", "password");
        long id = createAsAnna(anna);

        mockMvc.perform(put("/recipes/" + id).cookie(anna).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\" \",\"ingredients\":[{\"name\":\"bread\"}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Name is required"));
    }

    @Test
    void notLoggedInIsRejected() throws Exception {
        mockMvc.perform(put("/recipes/1").contentType(MediaType.APPLICATION_JSON).content(EDITED))
                .andExpect(status().isUnauthorized());
    }
}
