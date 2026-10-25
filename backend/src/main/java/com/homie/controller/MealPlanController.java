package com.homie.controller;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.homie.controller.dto.AuthDtos.ErrorResponse;
import com.homie.controller.dto.MealPlanDtos.SetMealRequest;
import com.homie.services.MealPlanService;

@RestController
@RequestMapping("/meal-plan")
public class MealPlanController {

    /** Most days one request may cover, a bit more than a year. */
    static final int MAX_DAYS = 400;

    private final MealPlanService mealPlanService;

    public MealPlanController(MealPlanService mealPlanService) {
        this.mealPlanService = mealPlanService;
    }

    @GetMapping
    public ResponseEntity<?> getEntries(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to, Authentication authentication) {
        if (from.isAfter(to)) {
            return ResponseEntity.badRequest().body(new ErrorResponse("The range must not end before it starts"));
        }
        if (ChronoUnit.DAYS.between(from, to) >= MAX_DAYS) {
            return ResponseEntity.badRequest().body(new ErrorResponse("At most " + MAX_DAYS + " days at once"));
        }
        return ResponseEntity.ok(mealPlanService.entries(authentication.getName(), from, to));
    }

    @PutMapping
    public ResponseEntity<?> setMeal(@RequestBody SetMealRequest request, Authentication authentication) {
        List<LocalDate> dates = request.dates();
        if (dates == null || dates.isEmpty() || dates.contains(null)) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Pick at least one day"));
        }
        if (dates.size() > MAX_DAYS) {
            return ResponseEntity.badRequest().body(new ErrorResponse("At most " + MAX_DAYS + " days at once"));
        }
        if (request.slot() == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Pick breakfast, lunch or dinner"));
        }
        return ResponseEntity.ok(mealPlanService.set(request, authentication.getName()));
    }
}
