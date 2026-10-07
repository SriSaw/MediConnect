package com.mediconnect.professional.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mediconnect.professional.Availability;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record AvailabilityResponse(
        Long id,
        Long professionalId,
        DayOfWeek dayOfWeek,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,
        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime,
        boolean available
) {
    public static AvailabilityResponse from(Availability availability) {
        if (availability == null) {
            return null;
        }
        return new AvailabilityResponse(
                availability.getId(),
                availability.getProfessional().getId(),
                availability.getDayOfWeek(),
                availability.getStartTime(),
                availability.getEndTime(),
                availability.isAvailable()
        );
    }
}
