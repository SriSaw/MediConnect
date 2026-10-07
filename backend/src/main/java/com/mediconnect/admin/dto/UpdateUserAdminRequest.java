package com.mediconnect.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserAdminRequest(
        @NotBlank(message = "Name cannot be blank")
        @Size(min = 2, max = 100)
        String name,

        @Email
        String email,

        String phone
) {}
