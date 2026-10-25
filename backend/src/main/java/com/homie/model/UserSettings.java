package com.homie.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** A user's personal preferences; users without a row get the defaults (see UserSettingsService). */
@Entity
@Table(name = "user_settings")
@Data
@NoArgsConstructor
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private AppUser user;

    @Column(nullable = false)
    private boolean useAi = true; // false: never ask Gemini for this user

    // the embeddable's columns are nullable, so Hibernate loads an all-null target as null
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "goal", column = @Column(name = "protein_goal")),
            @AttributeOverride(name = "min", column = @Column(name = "protein_min")),
            @AttributeOverride(name = "max", column = @Column(name = "protein_max")) })
    private MealTarget protein; // grams per meal

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "goal", column = @Column(name = "kcal_goal")),
            @AttributeOverride(name = "min", column = @Column(name = "kcal_min")),
            @AttributeOverride(name = "max", column = @Column(name = "kcal_max")) })
    private MealTarget calories; // kcal per meal

    public UserSettings(AppUser user) {
        this.user = user;
    }
}
