package com.homie.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.homie.model.AppUser;
import com.homie.model.MealPlanEntry;
import com.homie.model.MealSlot;

public interface MealPlanEntryRepository extends JpaRepository<MealPlanEntry, Long> {

    /** The user's entries from `from` to `to` (both inclusive), with their recipes. */
    @Query("select e from MealPlanEntry e join fetch e.recipe where e.user.username = :username and e.date between :from and :to")
    List<MealPlanEntry> findForUser(String username, LocalDate from, LocalDate to);

    List<MealPlanEntry> findByUserAndSlotAndDateIn(AppUser user, MealSlot slot, Collection<LocalDate> dates);

    /** All slots of the given dates, with their recipes. */
    @Query("select e from MealPlanEntry e join fetch e.recipe where e.user = :user and e.date in :dates")
    List<MealPlanEntry> findByUserAndDateIn(AppUser user, Collection<LocalDate> dates);
}
