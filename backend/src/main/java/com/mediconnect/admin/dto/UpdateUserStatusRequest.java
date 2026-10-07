package com.mediconnect.admin.dto;

import com.mediconnect.user.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
        @NotNull(message = "Status is required")
        UserStatus status
) {}
