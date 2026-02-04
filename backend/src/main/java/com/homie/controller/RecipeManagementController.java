package com.homie.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.homie.model.Recipe;
import com.homie.repository.RecipeRepository;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("management/recipes")
public class RecipeManagementController {

    @Autowired
    private RecipeRepository recipeRepository;

    @GetMapping
    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    @PostMapping
    public void registerNewRecipe(@RequestBody Recipe recipe) {
        System.out.println("added new recipe" + recipe.toString());
    }

    @DeleteMapping("{id}")
    public void deleteRecipe(@PathVariable("id") Integer id) {
        System.out.println(id);
    }

    @PutMapping("{id}")
    public void updateRecipe(Integer id, Recipe recipe) {
        System.out.println(String.format("%s %s", recipe, id));
    }

}
