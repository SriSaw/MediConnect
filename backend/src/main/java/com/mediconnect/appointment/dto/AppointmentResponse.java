package com.mediconnect.appointment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mediconnect.appointment.Appointment;
import com.mediconnect.appointment.AppointmentStatus;
import com.mediconnect.patient.dto.PatientProfileResponse;
import com.mediconnect.professional.dto.ProfessionalProfileResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AppointmentResponse(
        Long id,
        PatientProfileResponse patient,
        ProfessionalProfileResponse professional,
        LocalDate appointmentDate,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,
        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime,
        AppointmentStatus status,
        String reason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AppointmentResponse from(Appointment appointment) {
        if (appointment == null) {
            return null;
        }
        return new AppointmentResponse(
                appointment.getId(),
                PatientProfileResponse.from(appointment.getPatient()),
                ProfessionalProfileResponse.from(appointment.getProfessional()),
                appointment.getAppointmentDate(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus(),
                appointment.getReason(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }
}
