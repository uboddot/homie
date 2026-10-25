package com.homie.services;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.homie.controller.dto.MealPlanDtos.MealPlanEntryResponse;
import com.homie.controller.dto.MealPlanDtos.SetMealRequest;
import com.homie.model.AppUser;
import com.homie.model.MealPlanEntry;
import com.homie.model.Recipe;
import com.homie.repository.MealPlanEntryRepository;
import com.homie.repository.RecipeRepository;
import com.homie.repository.UserRepository;

@Service
public class MealPlanService {

    private static final Comparator<MealPlanEntryResponse> ORDER = Comparator
            .comparing(MealPlanEntryResponse::date).thenComparing(MealPlanEntryResponse::slot);

    private final MealPlanEntryRepository entryRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    public MealPlanService(MealPlanEntryRepository entryRepository, RecipeRepository recipeRepository,
            UserRepository userRepository) {
        this.entryRepository = entryRepository;
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
    }

    /** The user's planned meals from `from` to `to` (both inclusive), ordered by date and slot. */
    @Transactional(readOnly = true)
    public List<MealPlanEntryResponse> entries(String username, LocalDate from, LocalDate to) {
        return entryRepository.findForUser(username, from, to).stream().map(MealPlanService::toResponse)
                .sorted(ORDER).toList();
    }

    /**
     * Sets (or with recipeId null clears) one slot on every given date. The recipe must be the user's own or shared:
     * unknown → 404, someone else's private recipe → 403. Returns all entries of the given dates afterwards.
     */
    @Transactional
    public List<MealPlanEntryResponse> set(SetMealRequest request, String username) {
        // the cookie can outlive the user, e.g. after a dev restart recreated the DB
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in again"));
        Recipe recipe = request.recipeId() == null ? null : visibleRecipe(request.recipeId(), username);
        Set<LocalDate> dates = new TreeSet<>(request.dates());

        Map<LocalDate, MealPlanEntry> existing = entryRepository
                .findByUserAndSlotAndDateIn(user, request.slot(), dates).stream()
                .collect(Collectors.toMap(MealPlanEntry::getDate, Function.identity()));
        for (LocalDate date : dates) {
            MealPlanEntry entry = existing.get(date);
            if (recipe == null) {
                if (entry != null) {
                    entryRepository.delete(entry);
                }
            } else if (entry != null) {
                entry.setRecipe(recipe);
            } else {
                entryRepository.save(new MealPlanEntry(user, date, request.slot(), recipe));
            }
        }
        entryRepository.flush();

        return entryRepository.findByUserAndDateIn(user, dates).stream().map(MealPlanService::toResponse)
                .sorted(ORDER).toList();
    }

    private Recipe visibleRecipe(Long id, String username) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
        if (!recipe.isShared() && !username.equals(recipe.getOwnerUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only plan your own or shared recipes");
        }
        return recipe;
    }

    private static MealPlanEntryResponse toResponse(MealPlanEntry entry) {
        return new MealPlanEntryResponse(entry.getDate(), entry.getSlot(), entry.getRecipe().getId(),
                entry.getRecipe().getName());
    }
}
