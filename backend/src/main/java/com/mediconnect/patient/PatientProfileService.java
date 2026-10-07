package com.mediconnect.patient;

import com.mediconnect.audit.AuditLogService;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.patient.dto.PatientProfileRequest;
import com.mediconnect.patient.dto.PatientProfileResponse;
import com.mediconnect.security.UserPrincipal;
import com.mediconnect.user.User;
import com.mediconnect.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientProfileService {

    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public PatientProfileService(PatientProfileRepository patientProfileRepository,
                                 UserRepository userRepository,
                                 AuditLogService auditLogService) {
        this.patientProfileRepository = patientProfileRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PatientProfileResponse getMyProfile(UserPrincipal currentUser) {
        PatientProfile profile = patientProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("PatientProfile", "userId", currentUser.getId()));
        return PatientProfileResponse.from(profile);
    }

    @Transactional
    public PatientProfileResponse updateMyProfile(UserPrincipal currentUser, PatientProfileRequest request) {
        PatientProfile profile = patientProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("PatientProfile", "userId", currentUser.getId()));

        if (request.dateOfBirth() != null) {
            profile.setDateOfBirth(request.dateOfBirth());
        }
        if (request.gender() != null) {
            profile.setGender(request.gender().trim());
        }
        if (request.bloodGroup() != null) {
            profile.setBloodGroup(request.bloodGroup().trim());
        }
        if (request.address() != null) {
            profile.setAddress(request.address().trim());
        }
        if (request.emergencyContact() != null) {
            profile.setEmergencyContact(request.emergencyContact().trim());
        }

        User user = profile.getUser();
        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name().trim());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone().trim());
        }
        userRepository.save(user);

        PatientProfile updated = patientProfileRepository.save(profile);
        auditLogService.log(currentUser.getId(), "USER_UPDATED", "PATIENT", profile.getId().toString(), null);
        return PatientProfileResponse.from(updated);
    }

    @Transactional(readOnly = true)
    public PatientProfile getPatientProfileEntityByUserId(Long userId) {
        return patientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("PatientProfile", "userId", userId));
    }
}
