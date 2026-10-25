package com.homie.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.homie.controller.dto.SettingsDtos.MealTargetDto;
import com.homie.controller.dto.SettingsDtos.SettingsDto;
import com.homie.model.AppUser;
import com.homie.model.MealTarget;
import com.homie.model.UserSettings;
import com.homie.repository.UserRepository;
import com.homie.repository.UserSettingsRepository;

@Service
public class UserSettingsService {

    private final UserSettingsRepository settingsRepository;
    private final UserRepository userRepository;

    public UserSettingsService(UserSettingsRepository settingsRepository, UserRepository userRepository) {
        this.settingsRepository = settingsRepository;
        this.userRepository = userRepository;
    }

    /** The user's settings, or the defaults if they never saved any (nothing is written here). */
    @Transactional(readOnly = true)
    public SettingsDto get(String username) {
        return settingsRepository.findByUserUsername(username).map(SettingsDto::from).orElse(SettingsDto.DEFAULTS);
    }

    /** Whether Gemini may be asked on behalf of the user; on by default. */
    @Transactional(readOnly = true)
    public boolean useAi(String username) {
        return settingsRepository.findByUserUsername(username).map(UserSettings::isUseAi).orElse(true);
    }

    /** Replaces all settings of the user; the request is validated by the controller. */
    @Transactional
    public SettingsDto update(SettingsDto request, String username) {
        MealTarget protein = target(request.protein());
        MealTarget calories = target(request.calories());

        UserSettings settings = settingsRepository.findByUserUsername(username).orElseGet(() -> {
            // the cookie can outlive the user, e.g. after a dev restart recreated the DB
            AppUser user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in again"));
            return new UserSettings(user);
        });
        settings.setUseAi(request.useAi());
        settings.setProtein(protein);
        settings.setCalories(calories);
        return SettingsDto.from(settingsRepository.save(settings));
    }

    private static MealTarget target(MealTargetDto dto) {
        return dto == null ? null : new MealTarget(dto.goal(), dto.min(), dto.max());
    }
}
