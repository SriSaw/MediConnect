package com.mediconnect.professional;

import com.mediconnect.common.PageResponse;
import com.mediconnect.professional.dto.AvailabilityRequest;
import com.mediconnect.professional.dto.AvailabilityResponse;
import com.mediconnect.professional.dto.ProfessionalProfileRequest;
import com.mediconnect.professional.dto.ProfessionalProfileResponse;
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

import java.util.List;

@RestController
@RequestMapping("/api/professionals")
@Tag(name = "Professional", description = "Healthcare Professional and Availability endpoints")
public class ProfessionalController {

    private final ProfessionalService professionalService;

    public ProfessionalController(ProfessionalService professionalService) {
        this.professionalService = professionalService;
    }

    @GetMapping
    @Operation(summary = "Search healthcare professionals (public)")
    public ResponseEntity<PageResponse<ProfessionalProfileResponse>> getProfessionals(
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean verified,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(professionalService.getProfessionals(specialization, search, verified, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get professional by ID (public)")
    public ResponseEntity<ProfessionalProfileResponse> getProfessionalById(@PathVariable Long id) {
        return ResponseEntity.ok(professionalService.getProfessionalById(id));
    }

    @GetMapping("/{id}/availability")
    @Operation(summary = "Get professional availability schedules (public)")
    public ResponseEntity<List<AvailabilityResponse>> getProfessionalAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(professionalService.getProfessionalAvailability(id));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL')")
    @Operation(summary = "Get current professional profile")
    public ResponseEntity<ProfessionalProfileResponse> getMyProfile(@CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(professionalService.getMyProfile(currentUser));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL')")
    @Operation(summary = "Update current professional profile")
    public ResponseEntity<ProfessionalProfileResponse> updateMyProfile(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody ProfessionalProfileRequest request
    ) {
        return ResponseEntity.ok(professionalService.updateMyProfile(currentUser, request));
    }

    @GetMapping("/me/availability")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL')")
    @Operation(summary = "Get availability schedules for current professional")
    public ResponseEntity<List<AvailabilityResponse>> getMyAvailability(@CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(professionalService.getMyAvailability(currentUser));
    }

    @PostMapping("/me/availability")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL')")
    @Operation(summary = "Add an availability schedule")
    public ResponseEntity<AvailabilityResponse> addAvailability(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody AvailabilityRequest request
    ) {
        AvailabilityResponse response = professionalService.addAvailability(currentUser, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/me/availability/{id}")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL')")
    @Operation(summary = "Update an availability schedule")
    public ResponseEntity<AvailabilityResponse> updateAvailability(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody AvailabilityRequest request
    ) {
        return ResponseEntity.ok(professionalService.updateAvailability(currentUser, id, request));
    }

    @DeleteMapping("/me/availability/{id}")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL')")
    @Operation(summary = "Delete an availability schedule")
    public ResponseEntity<Void> deleteAvailability(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id
    ) {
        professionalService.deleteAvailability(currentUser, id);
        return ResponseEntity.noContent().build();
    }
}
