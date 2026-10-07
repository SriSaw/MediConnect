package com.mediconnect.patient;

import com.mediconnect.patient.dto.PatientProfileRequest;
import com.mediconnect.patient.dto.PatientProfileResponse;
import com.mediconnect.security.CurrentUser;
import com.mediconnect.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
@Tag(name = "Patient", description = "Patient profile management endpoints")
public class PatientController {

    private final PatientProfileService patientProfileService;

    public PatientController(PatientProfileService patientProfileService) {
        this.patientProfileService = patientProfileService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get current patient profile")
    public ResponseEntity<PatientProfileResponse> getMyProfile(@CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(patientProfileService.getMyProfile(currentUser));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Update current patient profile")
    public ResponseEntity<PatientProfileResponse> updateMyProfile(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody PatientProfileRequest request
    ) {
        return ResponseEntity.ok(patientProfileService.updateMyProfile(currentUser, request));
    }
}
