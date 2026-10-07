package com.mediconnect.appointment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateAppointmentRequest(
        @NotNull(message = "Professional ID is required")
        Long professionalId,

        @NotNull(message = "Appointment date is required")
        @FutureOrPresent(message = "Appointment date cannot be in the past")
        LocalDate appointmentDate,

        @NotNull(message = "Start time is required")
        @JsonFormat(pattern = "HH:mm[:ss]")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        @JsonFormat(pattern = "HH:mm[:ss]")
        LocalTime endTime,

        @NotBlank(message = "Reason for appointment is required")
        @Size(max = 1000, message = "Reason cannot exceed 1000 characters")
        String reason
) {}
