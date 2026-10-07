package com.mediconnect.admin.dto;

import com.mediconnect.user.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
        @NotNull(message = "Role is required")
        Role role
) {}
