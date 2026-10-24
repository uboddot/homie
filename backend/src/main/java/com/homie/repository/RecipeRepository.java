package com.homie.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.homie.model.Recipe;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    /** The user's own recipes plus every recipe someone shared. */
    @Query("select r from Recipe r left join fetch r.owner o where o.username = :username or r.shared = true")
    List<Recipe> findVisibleFor(String username);
}
