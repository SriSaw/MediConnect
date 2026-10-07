package com.mediconnect.appointment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mediconnect.appointment.AppointmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Detailed appointment report DTO populated by JDBC dynamic queries.
 */
public record AppointmentReportResponse(
        Long id,
        Long patientId,
        String patientName,
        String patientEmail,
        Long professionalId,
        String professionalName,
        String specialization,
        LocalDate appointmentDate,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,
        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime,
        AppointmentStatus status,
        String reason,
        BigDecimal consultationFee,
        LocalDateTime createdAt
) {}
