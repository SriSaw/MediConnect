package com.mediconnect.consultation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateConsultationRequest(
        @NotNull(message = "Appointment ID is required")
        Long appointmentId,

        @Size(max = 5000, message = "Notes cannot exceed 5000 characters")
        String notes,

        @NotBlank(message = "Medical advice is required")
        @Size(max = 5000, message = "Medical advice cannot exceed 5000 characters")
        String medicalAdvice,

        LocalDate followUpDate
) {}
