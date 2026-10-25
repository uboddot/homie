package com.homie.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** The recipe a user plans (or planned) for one meal of a day; at most one per user, date and slot. */
@Entity
@Table(name = "meal_plan_entry", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "plan_date", "slot" }))
@Data
@NoArgsConstructor
public class MealPlanEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(name = "plan_date", nullable = false) // "date" is a reserved word in H2
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MealSlot slot;

    @ToString.Exclude
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    public MealPlanEntry(AppUser user, LocalDate date, MealSlot slot, Recipe recipe) {
        this.user = user;
        this.date = date;
        this.slot = slot;
        this.recipe = recipe;
    }
}
