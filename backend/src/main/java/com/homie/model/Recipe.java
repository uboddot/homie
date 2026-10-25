package com.homie.model;

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import com.homie.nutrition.Nutrition;

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

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OrderBy("stepOrder")
    @OneToMany(mappedBy = "recipe", fetch = FetchType.LAZY)
    private List<Step> steps;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ElementCollection
    @CollectionTable(name = "recipe_ingredient", joinColumns = @JoinColumn(name = "recipe_id"))
    @OrderColumn(name = "position")
    private List<Ingredient> ingredients;

    private int preparationTime; // in minutes
    private int portions; // the ingredient amounts are for this many portions

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private AppUser owner;

    private boolean shared; // visible to all users, not only the owner

    @Embedded
    private Nutrition nutrition; // per portion, null if it couldn't be calculated

    public Recipe(String name, String description, List<Ingredient> ingredients, int preparationTime, int portions) {
        this.name = name;
        this.description = description;
        this.ingredients = ingredients;
        this.preparationTime = preparationTime;
        this.portions = portions;
    }

    public Recipe(String name, String description, List<Ingredient> ingredients, int preparationTime, int portions,
            AppUser owner, boolean shared) {
        this(name, description, ingredients, preparationTime, portions);
        this.owner = owner;
        this.shared = shared;
    }

    @JsonProperty("ownerUsername")
    public String getOwnerUsername() {
        return owner != null ? owner.getUsername() : null;
    }
}
