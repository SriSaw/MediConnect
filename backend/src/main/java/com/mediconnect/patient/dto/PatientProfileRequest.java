package com.mediconnect.patient.dto;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PatientProfileRequest(
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @Size(max = 30, message = "Phone cannot exceed 30 characters")
        String phone,

        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @Size(max = 20, message = "Gender cannot exceed 20 characters")
        String gender,

        @Size(max = 10, message = "Blood group cannot exceed 10 characters")
        String bloodGroup,

        @Size(max = 255, message = "Address cannot exceed 255 characters")
        String address,

        @Size(max = 100, message = "Emergency contact cannot exceed 100 characters")
        String emergencyContact
) {}
