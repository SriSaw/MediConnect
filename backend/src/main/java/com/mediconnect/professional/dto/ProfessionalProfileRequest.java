package com.mediconnect.professional.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProfessionalProfileRequest(
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @Size(max = 30, message = "Phone cannot exceed 30 characters")
        String phone,

        @Size(max = 100, message = "Specialization cannot exceed 100 characters")
        String specialization,

        @Size(max = 100, message = "License number cannot exceed 100 characters")
        String licenseNumber,

        @Min(value = 0, message = "Experience years cannot be negative")
        Integer experienceYears,

        @Size(max = 2000, message = "Bio cannot exceed 2000 characters")
        String bio,

        @DecimalMin(value = "0.00", message = "Consultation fee cannot be negative")
        BigDecimal consultationFee
) {}
