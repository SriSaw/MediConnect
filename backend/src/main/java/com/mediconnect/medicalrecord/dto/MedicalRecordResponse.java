package com.mediconnect.medicalrecord.dto;

import com.mediconnect.medicalrecord.MedicalRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MedicalRecordResponse(
        Long id,
        Long patientId,
        String patientName,
        String recordType,
        String title,
        String description,
        LocalDate recordDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static MedicalRecordResponse from(MedicalRecord record) {
        if (record == null) {
            return null;
        }
        return new MedicalRecordResponse(
                record.getId(),
                record.getPatient().getId(),
                record.getPatient().getUser().getName(),
                record.getRecordType(),
                record.getTitle(),
                record.getDescription(),
                record.getRecordDate(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }
}
