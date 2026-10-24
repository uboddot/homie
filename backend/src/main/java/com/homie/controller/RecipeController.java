package com.homie.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.homie.repository.RecipeRepository;
import com.homie.model.Recipe;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/recipes")
public class RecipeController {

    @Autowired
    private RecipeRepository recipeRepository;

    @GetMapping
    public ResponseEntity<List<Recipe>> getRecipes(Authentication authentication) {
        return new ResponseEntity<List<Recipe>>(recipeRepository.findVisibleFor(authentication.getName()), HttpStatus.OK);
    }

    @GetMapping("/getRecipeById")
    public ResponseEntity<Recipe> getRecipeById(@RequestParam Long recipeId) {
        return new ResponseEntity<Recipe>(recipeRepository.findById(recipeId).orElse(null), HttpStatus.OK);
    }
}
