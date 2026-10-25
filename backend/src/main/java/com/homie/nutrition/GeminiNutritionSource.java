package com.homie.nutrition;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import com.homie.model.IngredientUnit;

import tools.jackson.databind.json.JsonMapper;

/**
 * Asks Gemini for the macros and unit weights the cache is missing, all in one structured (JSON) request.
 * Without GOOGLE_API_KEY, or if the request fails, it answers nothing; invalid entries are dropped.
 */
@Component
public class GeminiNutritionSource implements NutritionSource {

    private static final Logger log = LoggerFactory.getLogger(GeminiNutritionSource.class);
    private static final String MODEL = "gemini-3-flash-preview";
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private static final GenerateContentConfig CONFIG = GenerateContentConfig.builder()
            .responseMimeType("application/json")
            .responseSchema(object(Map.of(
                    "ingredients", array(object(Map.of(
                            "name", type(Type.Known.STRING),
                            "fatPer100g", type(Type.Known.NUMBER),
                            "carbsPer100g", type(Type.Known.NUMBER),
                            "proteinPer100g", type(Type.Known.NUMBER)))),
                    "unitWeights", array(object(Map.of(
                            "name", type(Type.Known.STRING),
                            "unit", type(Type.Known.STRING),
                            "grams", type(Type.Known.NUMBER)))))))
            .build();

    private static final String PROMPT = """
            You are a nutrition database for a recipe app.
            1. For each ingredient under "Ingredients", give fat, carbohydrates and protein in grams per 100 g of the
               ingredient as it is typically bought for cooking (raw, unless the name says otherwise).
            2. For each "name | unit" pair under "Unit weights", give the typical weight in grams of ONE unit of that
               ingredient. Units: ML, CL, DL, L (volume), TSP (teaspoon), TBSP (tablespoon), CUP (US cup, 240 ml),
               PINCH, PIECE (one whole item), CLOVE, SLICE, CAN (a standard can, drained weight not subtracted), BUNCH.
            Names can be in any language. Copy every name and unit exactly as given.

            Ingredients:
            %s
            Unit weights:
            %s
            """;

    /** The JSON Gemini answers with, see CONFIG. */
    record Answer(List<IngredientAnswer> ingredients, List<UnitWeightAnswer> unitWeights) {
    }

    record IngredientAnswer(String name, BigDecimal fatPer100g, BigDecimal carbsPer100g, BigDecimal proteinPer100g) {
    }

    record UnitWeightAnswer(String name, String unit, BigDecimal grams) {
    }

    private final Client client; // null without API key
    private final JsonMapper jsonMapper;

    public GeminiNutritionSource(JsonMapper jsonMapper) {
        String apiKey = System.getenv("GOOGLE_API_KEY");
        this.client = apiKey == null || apiKey.isBlank() ? null : new Client.Builder().apiKey(apiKey).build();
        this.jsonMapper = jsonMapper;
    }

    @Override
    public Fetched fetch(Set<String> names, Set<UnitKey> unitWeights) {
        if (client == null || (names.isEmpty() && unitWeights.isEmpty())) {
            return Fetched.EMPTY;
        }
        try {
            String prompt = PROMPT.formatted(
                    lines(names.stream().toList()),
                    lines(unitWeights.stream().map(key -> key.name() + " | " + key.unit().name()).toList()));
            String json = client.models.generateContent(MODEL, prompt, CONFIG).text();
            return toFetched(jsonMapper.readValue(json, Answer.class), names, unitWeights);
        } catch (RuntimeException e) {
            log.warn("Could not get nutrition data from Gemini", e);
            return Fetched.EMPTY;
        }
    }

    /** Keeps only answers to what was asked, with plausible values; macros are converted to per gram. */
    private static Fetched toFetched(Answer answer, Set<String> names, Set<UnitKey> unitWeights) {
        List<IngredientNutrition> ingredients = new ArrayList<>();
        for (IngredientAnswer a : answer.ingredients() == null ? List.<IngredientAnswer>of() : answer.ingredients()) {
            if (a == null || a.name() == null || !names.contains(IngredientNutritionCache.normalize(a.name()))) {
                continue;
            }
            BigDecimal fat = perGram(a.fatPer100g());
            BigDecimal carbs = perGram(a.carbsPer100g());
            BigDecimal protein = perGram(a.proteinPer100g());
            if (fat != null || carbs != null || protein != null) {
                ingredients.add(new IngredientNutrition(IngredientNutritionCache.normalize(a.name()), fat, carbs, protein));
            } else {
                log.warn("Gemini gave no usable macros for {}", a.name());
            }
        }

        List<IngredientUnitWeight> weights = new ArrayList<>();
        for (UnitWeightAnswer a : answer.unitWeights() == null ? List.<UnitWeightAnswer>of() : answer.unitWeights()) {
            if (a == null || a.name() == null || a.unit() == null || a.grams() == null || a.grams().signum() <= 0) {
                continue;
            }
            UnitKey key;
            try {
                key = new UnitKey(IngredientNutritionCache.normalize(a.name()), IngredientUnit.valueOf(a.unit().trim()));
            } catch (IllegalArgumentException e) {
                continue;
            }
            if (unitWeights.contains(key)) {
                weights.add(new IngredientUnitWeight(key.name(), key.unit(), a.grams()));
            }
        }
        return new Fetched(ingredients, weights);
    }

    /** Grams per 100 g → grams per gram; null if missing or impossible. */
    private static BigDecimal perGram(BigDecimal per100g) {
        if (per100g == null || per100g.signum() < 0 || per100g.compareTo(HUNDRED) > 0) {
            return null;
        }
        return per100g.divide(HUNDRED);
    }

    private static String lines(List<String> values) {
        return values.isEmpty() ? "(none)\n" : String.join("\n", values.stream().map(v -> "- " + v).toList()) + "\n";
    }

    private static Schema type(Type.Known type) {
        return Schema.builder().type(type).build();
    }

    private static Schema array(Schema items) {
        return Schema.builder().type(Type.Known.ARRAY).items(items).build();
    }

    /** Object with all given properties required. */
    private static Schema object(Map<String, Schema> properties) {
        return Schema.builder().type(Type.Known.OBJECT).properties(properties)
                .required(List.copyOf(properties.keySet())).build();
    }
}
