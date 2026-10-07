package com.mediconnect.medicalrecord;

import com.mediconnect.common.PageResponse;
import com.mediconnect.medicalrecord.dto.MedicalRecordRequest;
import com.mediconnect.medicalrecord.dto.MedicalRecordResponse;
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
@RequestMapping("/api")
@Tag(name = "MedicalRecord", description = "Personal health records and authorized clinical access endpoints")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    @GetMapping("/patients/me/records")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get current patient's medical records")
    public ResponseEntity<PageResponse<MedicalRecordResponse>> getMyRecords(
            @CurrentUser UserPrincipal currentUser,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(medicalRecordService.getMyRecords(currentUser, pageable));
    }

    @PostMapping("/patients/me/records")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Create a medical record for current patient")
    public ResponseEntity<MedicalRecordResponse> createMyRecord(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody MedicalRecordRequest request
    ) {
        MedicalRecordResponse response = medicalRecordService.createMyRecord(currentUser, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/patients/me/records/{id}")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get a medical record by ID for current patient")
    public ResponseEntity<MedicalRecordResponse> getMyRecordById(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(medicalRecordService.getMyRecordById(currentUser, id));
    }

    @PutMapping("/patients/me/records/{id}")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Update a medical record for current patient")
    public ResponseEntity<MedicalRecordResponse> updateMyRecord(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody MedicalRecordRequest request
    ) {
        return ResponseEntity.ok(medicalRecordService.updateMyRecord(currentUser, id, request));
    }

    @DeleteMapping("/patients/me/records/{id}")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Delete a medical record for current patient")
    public ResponseEntity<Void> deleteMyRecord(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id
    ) {
        medicalRecordService.deleteMyRecord(currentUser, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/professionals/patients/{patientId}/records")
    @PreAuthorize("hasAnyRole('HEALTHCARE_PROFESSIONAL', 'ADMIN')")
    @Operation(summary = "Access authorized patient medical records (Audited)")
    public ResponseEntity<PageResponse<MedicalRecordResponse>> getPatientRecordsByProfessional(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long patientId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(medicalRecordService.getPatientRecordsByProfessional(currentUser, patientId, pageable));
    }
}
