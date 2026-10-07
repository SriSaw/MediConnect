package com.mediconnect.patient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mediconnect.patient.PatientProfile;
import com.mediconnect.user.dto.UserResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PatientProfileResponse(
        Long id,
        UserResponse user,
        LocalDate dateOfBirth,
        String gender,
        String bloodGroup,
        String address,
        String emergencyContact,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PatientProfileResponse from(PatientProfile profile) {
        if (profile == null) {
            return null;
        }
        return new PatientProfileResponse(
                profile.getId(),
                UserResponse.from(profile.getUser()),
                profile.getDateOfBirth(),
                profile.getGender(),
                profile.getBloodGroup(),
                profile.getAddress(),
                profile.getEmergencyContact(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    @JsonProperty("userId")
    public Long getUserId() {
        return user != null ? user.id() : null;
    }

    @JsonProperty("userName")
    public String getUserName() {
        return user != null ? user.name() : null;
    }

    @JsonProperty("email")
    public String getEmail() {
        return user != null ? user.email() : null;
    }

    @JsonProperty("phone")
    public String getPhone() {
        return user != null ? user.phone() : null;
    }
}
