package com.mediconnect.admin.dto;

import jakarta.validation.constraints.NotNull;

public record VerifyProfessionalRequest(
        @NotNull(message = "Verified flag is required")
        Boolean verified
) {}
