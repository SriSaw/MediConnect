package com.mediconnect.appointment.dto;

import jakarta.validation.constraints.Size;

public record CancelAppointmentRequest(
        @Size(max = 500, message = "Cancellation reason cannot exceed 500 characters")
        String reason
) {}
