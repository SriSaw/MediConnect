package com.mediconnect.consultation.dto;

import com.mediconnect.consultation.Consultation;
import com.mediconnect.patient.dto.PatientProfileResponse;
import com.mediconnect.professional.dto.ProfessionalProfileResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ConsultationResponse(
        Long id,
        Long appointmentId,
        LocalDate appointmentDate,
        PatientProfileResponse patient,
        ProfessionalProfileResponse professional,
        String notes,
        String medicalAdvice,
        LocalDate followUpDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ConsultationResponse from(Consultation consultation) {
        if (consultation == null) {
            return null;
        }
        return new ConsultationResponse(
                consultation.getId(),
                consultation.getAppointment().getId(),
                consultation.getAppointment().getAppointmentDate(),
                PatientProfileResponse.from(consultation.getPatient()),
                ProfessionalProfileResponse.from(consultation.getProfessional()),
                consultation.getNotes(),
                consultation.getMedicalAdvice(),
                consultation.getFollowUpDate(),
                consultation.getCreatedAt(),
                consultation.getUpdatedAt()
        );
    }
}
