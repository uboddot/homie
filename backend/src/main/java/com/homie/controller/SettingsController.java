package com.homie.controller;

import java.util.stream.Stream;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.homie.controller.dto.AuthDtos.ErrorResponse;
import com.homie.controller.dto.SettingsDtos.MealTargetDto;
import com.homie.controller.dto.SettingsDtos.SettingsDto;
import com.homie.services.UserSettingsService;

@RestController
@RequestMapping("/settings")
public class SettingsController {

    private final UserSettingsService settingsService;

    public SettingsController(UserSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public SettingsDto getSettings(Authentication authentication) {
        return settingsService.get(authentication.getName());
    }

    @PutMapping
    public ResponseEntity<?> updateSettings(@RequestBody SettingsDto request, Authentication authentication) {
        for (MealTargetDto target : new MealTargetDto[] { request.protein(), request.calories() }) {
            String invalid = validate(target);
            if (invalid != null) {
                return ResponseEntity.badRequest().body(new ErrorResponse(invalid));
            }
        }
        return ResponseEntity.ok(settingsService.update(request, authentication.getName()));
    }

    /** The error message if the target can't be saved, otherwise null. */
    private static String validate(MealTargetDto target) {
        if (target == null) {
            return null;
        }
        if (target.goal() != null && (target.min() != null || target.max() != null)) {
            return "Choose either a goal or a range";
        }
        if (Stream.of(target.goal(), target.min(), target.max()).anyMatch(v -> v != null && v.signum() <= 0)) {
            return "Targets must be positive";
        }
        if (target.min() != null && target.max() != null && target.min().compareTo(target.max()) > 0) {
            return "The minimum can't be above the maximum";
        }
        return null;
    }
}
