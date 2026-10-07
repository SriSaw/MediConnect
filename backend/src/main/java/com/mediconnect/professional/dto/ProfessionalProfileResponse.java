package com.mediconnect.professional.dto;

import com.mediconnect.professional.ProfessionalProfile;
import com.mediconnect.user.dto.UserResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProfessionalProfileResponse(
        Long id,
        UserResponse user,
        String specialization,
        String licenseNumber,
        Integer experienceYears,
        String bio,
        BigDecimal consultationFee,
        boolean verified,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProfessionalProfileResponse from(ProfessionalProfile profile) {
        if (profile == null) {
            return null;
        }
        return new ProfessionalProfileResponse(
                profile.getId(),
                UserResponse.from(profile.getUser()),
                profile.getSpecialization(),
                profile.getLicenseNumber(),
                profile.getExperienceYears(),
                profile.getBio(),
                profile.getConsultationFee(),
                profile.isVerified(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
