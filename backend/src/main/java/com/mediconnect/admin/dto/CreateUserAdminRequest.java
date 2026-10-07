package com.mediconnect.admin.dto;

import com.mediconnect.user.Role;
import com.mediconnect.user.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserAdminRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100)
        String name,

        @NotBlank(message = "Email is required")
        @Email
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100)
        String password,

        String phone,

        @NotNull(message = "Role is required")
        Role role,

        UserStatus status
) {}
