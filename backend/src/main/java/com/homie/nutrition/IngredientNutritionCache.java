package com.homie.nutrition;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.homie.model.Ingredient;
import com.homie.model.IngredientUnit;
import com.homie.nutrition.NutritionSource.Fetched;
import com.homie.nutrition.NutritionSource.UnitKey;

/** Per-ingredient macros and unit weights from the DB; only what isn't stored yet is asked from the source. */
@Service
public class IngredientNutritionCache {

    private final IngredientNutritionRepository nutritionRepository;
    private final IngredientUnitWeightRepository unitWeightRepository;
    private final NutritionSource source;

    public IngredientNutritionCache(IngredientNutritionRepository nutritionRepository,
            IngredientUnitWeightRepository unitWeightRepository, NutritionSource source) {
        this.nutritionRepository = nutritionRepository;
        this.unitWeightRepository = unitWeightRepository;
        this.source = source;
    }

    /** What is known about the ingredients of a recipe; a missing entry means unknown. */
    public record Lookup(Map<String, IngredientNutrition> nutrition, Map<UnitKey, BigDecimal> unitWeights) {

        public IngredientNutrition nutrition(Ingredient ingredient) {
            return nutrition.get(normalize(ingredient.getName()));
        }

        /** Grams of one unit of the ingredient, null if unknown. */
        public BigDecimal gramsPerUnit(Ingredient ingredient) {
            return unitWeights.get(unitKey(ingredient));
        }
    }

    /** "  Red   Onion " → "red onion", so the same ingredient hits the same cache row. */
    public static String normalize(String name) {
        return name.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** Unit weights are needed for every unit but G and KG; an amount without unit counts pieces ("2 eggs"). */
    static boolean needsUnitWeight(Ingredient ingredient) {
        return ingredient.getUnit() != IngredientUnit.G && ingredient.getUnit() != IngredientUnit.KG;
    }

    static UnitKey unitKey(Ingredient ingredient) {
        IngredientUnit unit = ingredient.getUnit() == null ? IngredientUnit.PIECE : ingredient.getUnit();
        return new UnitKey(normalize(ingredient.getName()), unit);
    }

    /** Loads the cached data for all ingredients with an amount; asks the source once for the rest and stores it. */
    @Transactional
    public Lookup lookup(List<Ingredient> ingredients) {
        List<Ingredient> measured = ingredients.stream().filter(i -> i.getAmount() != null).toList();
        Set<String> names = new HashSet<>(measured.stream().map(i -> normalize(i.getName())).toList());
        Set<UnitKey> units = new HashSet<>(measured.stream()
                .filter(IngredientNutritionCache::needsUnitWeight)
                .map(IngredientNutritionCache::unitKey)
                .toList());

        Map<String, IngredientNutrition> nutrition = new HashMap<>();
        nutritionRepository.findByNameIn(names).forEach(n -> nutrition.put(n.getName(), n));
        Map<UnitKey, BigDecimal> unitWeights = new HashMap<>();
        unitWeightRepository.findByNameIn(names).forEach(w -> unitWeights.put(new UnitKey(w.getName(), w.getUnit()), w.getGrams()));

        Set<String> missingNames = new HashSet<>(names);
        missingNames.removeAll(nutrition.keySet());
        Set<UnitKey> missingUnits = new HashSet<>(units);
        missingUnits.removeAll(unitWeights.keySet());
        if (missingNames.isEmpty() && missingUnits.isEmpty()) {
            return new Lookup(nutrition, unitWeights);
        }

        Fetched fetched = source.fetch(missingNames, missingUnits);
        if (fetched == null) {
            fetched = Fetched.EMPTY;
        }
        for (IngredientNutrition n : fetched.ingredients()) {
            if (missingNames.remove(n.getName())) {
                nutrition.put(n.getName(), nutritionRepository.save(n));
            }
        }
        for (IngredientUnitWeight w : fetched.unitWeights()) {
            UnitKey key = new UnitKey(w.getName(), w.getUnit());
            if (missingUnits.remove(key)) {
                unitWeights.put(key, unitWeightRepository.save(w).getGrams());
            }
        }
        return new Lookup(nutrition, unitWeights);
    }
}
