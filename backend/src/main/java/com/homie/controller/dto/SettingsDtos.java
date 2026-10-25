package com.homie.controller.dto;

import java.math.BigDecimal;

import com.homie.model.MealTarget;
import com.homie.model.UserSettings;

public final class SettingsDtos {

    private SettingsDtos() {
    }

    /** Either goal, or min and/or max; all null (or the whole target null) means no target. */
    public record MealTargetDto(BigDecimal goal, BigDecimal min, BigDecimal max) {

        static MealTargetDto from(MealTarget target) {
            if (target == null || (target.getGoal() == null && target.getMin() == null && target.getMax() == null)) {
                return null;
            }
            return new MealTargetDto(target.getGoal(), target.getMin(), target.getMax());
        }
    }

    /** GET /settings answer and PUT /settings body (replaces everything). */
    public record SettingsDto(boolean useAi, MealTargetDto protein, MealTargetDto calories) {

        public static final SettingsDto DEFAULTS = new SettingsDto(true, null, null);

        public static SettingsDto from(UserSettings settings) {
            return new SettingsDto(settings.isUseAi(), MealTargetDto.from(settings.getProtein()),
                    MealTargetDto.from(settings.getCalories()));
        }
    }
}
