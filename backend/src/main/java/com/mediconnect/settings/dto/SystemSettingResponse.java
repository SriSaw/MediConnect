package com.mediconnect.settings.dto;

import com.mediconnect.settings.SystemSetting;

import java.time.LocalDateTime;

public record SystemSettingResponse(
        Long id,
        String key,
        String value,
        String description,
        LocalDateTime updatedAt
) {
    public static SystemSettingResponse from(SystemSetting setting) {
        if (setting == null) {
            return null;
        }
        return new SystemSettingResponse(
                setting.getId(),
                setting.getKey(),
                setting.getValue(),
                setting.getDescription(),
                setting.getUpdatedAt()
        );
    }
}
