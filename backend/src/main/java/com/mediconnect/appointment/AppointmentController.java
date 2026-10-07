package com.mediconnect.appointment;

import com.mediconnect.appointment.dto.AppointmentResponse;
import com.mediconnect.appointment.dto.CancelAppointmentRequest;
import com.mediconnect.appointment.dto.CreateAppointmentRequest;
import com.mediconnect.common.PageResponse;
import com.mediconnect.security.CurrentUser;
import com.mediconnect.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
@Tag(name = "Appointment", description = "Appointment booking, management, and cancellation endpoints")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/appointments")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Book a new appointment (Patient)")
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateAppointmentRequest request
    ) {
        AppointmentResponse response = appointmentService.bookAppointment(currentUser, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/appointments/me")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get upcoming and past appointments for current patient")
    public ResponseEntity<PageResponse<AppointmentResponse>> getMyAppointments(
            @CurrentUser UserPrincipal currentUser,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(appointmentService.getPatientAppointments(currentUser, pageable));
    }

    @GetMapping("/appointments/{id}")
    @Operation(summary = "Get appointment by ID")
    public ResponseEntity<AppointmentResponse> getAppointmentById(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(currentUser, id));
    }

    @PatchMapping("/appointments/{id}/cancel")
    @Operation(summary = "Cancel an appointment")
    public ResponseEntity<AppointmentResponse> cancelAppointment(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id,
            @RequestBody(required = false) CancelAppointmentRequest request
    ) {
        String reason = request != null ? request.reason() : null;
        return ResponseEntity.ok(appointmentService.cancelAppointment(currentUser, id, reason));
    }

    @GetMapping("/professionals/me/appointments")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL')")
    @Operation(summary = "Get appointments for current healthcare professional")
    public ResponseEntity<PageResponse<AppointmentResponse>> getProfessionalAppointments(
            @CurrentUser UserPrincipal currentUser,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(appointmentService.getProfessionalAppointments(currentUser, pageable));
    }

    @GetMapping("/admin/appointments")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: list appointments with optional filters")
    public ResponseEntity<PageResponse<AppointmentResponse>> getAdminAppointments(
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(appointmentService.getAllAppointments(status, date, pageable));
    }

    @PatchMapping("/admin/appointments/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: cancel an appointment")
    public ResponseEntity<AppointmentResponse> adminCancelAppointment(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id,
            @RequestBody(required = false) CancelAppointmentRequest request
    ) {
        String reason = request != null ? request.reason() : null;
        return ResponseEntity.ok(appointmentService.cancelAppointment(currentUser, id, reason));
    }

    @GetMapping("/admin/appointments/reports")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: JDBC-powered detailed appointment reporting with multi-table joins and filtering")
    public ResponseEntity<PageResponse<com.mediconnect.appointment.dto.AppointmentReportResponse>> getAppointmentReports(
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long professionalId,
            @RequestParam(required = false) Long patientId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(appointmentService.searchAppointmentReports(
                status, startDate, endDate, professionalId, patientId, pageable
        ));
    }
}
