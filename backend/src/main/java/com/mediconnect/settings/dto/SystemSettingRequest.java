package com.mediconnect.settings.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SystemSettingRequest(
        @NotBlank(message = "Setting value cannot be blank")
        @Size(max = 255, message = "Setting value cannot exceed 255 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_.,\\-\\s]+$", message = "Setting value contains invalid characters")
        String value,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description
) {}
