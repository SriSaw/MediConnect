package com.mediconnect.consultation;

import com.mediconnect.common.PageResponse;
import com.mediconnect.consultation.dto.ConsultationResponse;
import com.mediconnect.consultation.dto.CreateConsultationRequest;
import com.mediconnect.consultation.dto.UpdateConsultationRequest;
import com.mediconnect.security.CurrentUser;
import com.mediconnect.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consultations")
@Tag(name = "Consultation", description = "Consultation notes and medical advice endpoints")
public class ConsultationController {

    private final ConsultationService consultationService;

    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL')")
    @Operation(summary = "Create consultation and record medical advice (Professional only)")
    public ResponseEntity<ConsultationResponse> createConsultation(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateConsultationRequest request
    ) {
        ConsultationResponse response = consultationService.createConsultation(currentUser, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    @Operation(summary = "Get consultations for current user (Patient or Professional)")
    public ResponseEntity<PageResponse<ConsultationResponse>> getMyConsultations(
            @CurrentUser UserPrincipal currentUser,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(consultationService.getMyConsultations(currentUser, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get consultation by ID")
    public ResponseEntity<ConsultationResponse> getConsultationById(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(consultationService.getConsultationById(currentUser, id));
    }

    @GetMapping("/appointment/{appointmentId}")
    @Operation(summary = "Get consultation for a specific appointment")
    public ResponseEntity<ConsultationResponse> getConsultationByAppointmentId(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long appointmentId
    ) {
        return ResponseEntity.ok(consultationService.getConsultationByAppointmentId(currentUser, appointmentId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL')")
    @Operation(summary = "Update consultation notes and medical advice (Assigned Professional only)")
    public ResponseEntity<ConsultationResponse> updateConsultation(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdateConsultationRequest request
    ) {
        return ResponseEntity.ok(consultationService.updateConsultation(currentUser, id, request));
    }
}
