package com.mediconnect.appointment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediconnect.appointment.dto.CreateAppointmentRequest;
import com.mediconnect.professional.dto.AvailabilityRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AppointmentFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Verify public professional search and availability lookup")
    void testPublicProfessionalLookup() throws Exception {
        mockMvc.perform(get("/api/professionals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", not(empty())));

        mockMvc.perform(get("/api/professionals/1/availability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    @WithUserDetails("patient1@mediconnect.local")
    @DisplayName("Verify Patient Appointment Booking flow with valid and invalid slots")
    void testPatientAppointmentBookingFlow() throws Exception {
        // Target next Monday at least 14 days ahead
        LocalDate targetDate = LocalDate.now().plusDays(14);
        while (targetDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            targetDate = targetDate.plusDays(1);
        }

        // 1. Successful booking within Dr Jenkins Monday 09:00 - 13:00 window
        CreateAppointmentRequest validReq = new CreateAppointmentRequest(
                1L,
                targetDate,
                LocalTime.of(9, 30),
                LocalTime.of(10, 0),
                "Consultation on recurring headaches"
        );

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.status", is("CONFIRMED")));

        // 2. Conflict booking: attempt to double-book same slot
        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode", is("CONFLICT")))
                .andExpect(jsonPath("$.message", containsString("already booked")));

        // 3. Outside availability: attempt to book at 23:00
        CreateAppointmentRequest outsideReq = new CreateAppointmentRequest(
                1L,
                targetDate,
                LocalTime.of(23, 0),
                LocalTime.of(23, 30),
                "Late night"
        );

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(outsideReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("not within the healthcare professional's available hours")));
    }

    @Test
    @WithUserDetails("doctor1@mediconnect.local")
    @DisplayName("Verify Professional Availability Flow: create valid and conflicting windows")
    void testDoctorAvailabilityFlow() throws Exception {
        // Dr 1 does not have Saturday hours seeded
        AvailabilityRequest satReq = new AvailabilityRequest(
                DayOfWeek.SATURDAY,
                LocalTime.of(10, 0),
                LocalTime.of(14, 0),
                true
        );

        // 1. Add Saturday window
        mockMvc.perform(post("/api/professionals/me/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(satReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.dayOfWeek", is("SATURDAY")));

        // 2. Attempt overlapping window on Saturday (11:00 - 13:00)
        AvailabilityRequest overlapReq = new AvailabilityRequest(
                DayOfWeek.SATURDAY,
                LocalTime.of(11, 0),
                LocalTime.of(13, 0),
                true
        );

        mockMvc.perform(post("/api/professionals/me/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overlapReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode", is("CONFLICT")))
                .andExpect(jsonPath("$.message", containsString("overlaps with existing availability")));

        // 3. Attempt invalid window where start >= end
        AvailabilityRequest invalidTimeReq = new AvailabilityRequest(
                DayOfWeek.SATURDAY,
                LocalTime.of(15, 0),
                LocalTime.of(14, 0),
                true
        );

        mockMvc.perform(post("/api/professionals/me/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTimeReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("must be before end time")));
    }
}
