package com.homie.model;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recipe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    
    @OneToMany(mappedBy = "id")
    private List<Step> steps;

    private List<String> ingredients;
    private int preparationTime; // in minutes

    public Recipe(String name, String description, List<String> ingredients, int preparationTime) {
        this.name = name;
        this.description = description;
        this.ingredients = ingredients;
        this.preparationTime = preparationTime;

        this.steps = List.of(new Step(1, "Preheat the oven to 350°F (175°C)."),
                             new Step(2, "Mix all ingredients in a bowl."),
                             new Step(3, "Pour the mixture into a baking dish."),
                             new Step(4, "Bake for 30 minutes or until golden brown."));

    }
}
