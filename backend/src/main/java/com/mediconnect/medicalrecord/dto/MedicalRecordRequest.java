package com.mediconnect.medicalrecord.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record MedicalRecordRequest(
        @NotBlank(message = "Record type is required")
        @Size(max = 50, message = "Record type cannot exceed 50 characters")
        String recordType,

        @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title cannot exceed 150 characters")
        String title,

        @NotBlank(message = "Description is required")
        @Size(max = 5000, message = "Description cannot exceed 5000 characters")
        String description,

        @NotNull(message = "Record date is required")
        @PastOrPresent(message = "Record date cannot be in the future")
        LocalDate recordDate
) {}
