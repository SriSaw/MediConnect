package com.mediconnect.medicalrecord;

import com.mediconnect.appointment.AppointmentRepository;
import com.mediconnect.appointment.AppointmentStatus;
import com.mediconnect.audit.AuditLogService;
import com.mediconnect.common.PageResponse;
import com.mediconnect.exception.ForbiddenException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.medicalrecord.dto.MedicalRecordRequest;
import com.mediconnect.medicalrecord.dto.MedicalRecordResponse;
import com.mediconnect.patient.PatientProfile;
import com.mediconnect.patient.PatientProfileRepository;
import com.mediconnect.professional.ProfessionalProfile;
import com.mediconnect.professional.ProfessionalProfileRepository;
import com.mediconnect.security.UserPrincipal;
import com.mediconnect.user.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class MedicalRecordService {

    private static final Set<AppointmentStatus> VALID_RELATIONSHIP_STATUSES = Set.of(
            AppointmentStatus.PENDING,
            AppointmentStatus.CONFIRMED,
            AppointmentStatus.COMPLETED
    );

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuditLogService auditLogService;

    public MedicalRecordService(
            MedicalRecordRepository medicalRecordRepository,
            PatientProfileRepository patientProfileRepository,
            ProfessionalProfileRepository professionalProfileRepository,
            AppointmentRepository appointmentRepository,
            AuditLogService auditLogService
    ) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.professionalProfileRepository = professionalProfileRepository;
        this.appointmentRepository = appointmentRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<MedicalRecordResponse> getMyRecords(UserPrincipal currentUser, Pageable pageable) {
        PatientProfile patient = patientProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("PatientProfile", "userId", currentUser.getId()));

        Page<MedicalRecord> page = medicalRecordRepository.findByPatientIdOrderByRecordDateDescCreatedAtDesc(
                patient.getId(), pageable
        );
        return PageResponse.from(page.map(MedicalRecordResponse::from));
    }

    @Transactional
    public MedicalRecordResponse createMyRecord(UserPrincipal currentUser, MedicalRecordRequest request) {
        PatientProfile patient = patientProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("PatientProfile", "userId", currentUser.getId()));

        MedicalRecord record = new MedicalRecord(
                patient,
                request.recordType().trim(),
                request.title().trim(),
                request.description().trim(),
                request.recordDate()
        );

        MedicalRecord saved = medicalRecordRepository.save(record);
        auditLogService.log(currentUser.getId(), "MEDICAL_RECORD_CREATED", "MEDICAL_RECORD", saved.getId().toString(), null);
        return MedicalRecordResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public MedicalRecordResponse getMyRecordById(UserPrincipal currentUser, Long id) {
        PatientProfile patient = patientProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("PatientProfile", "userId", currentUser.getId()));

        MedicalRecord record = medicalRecordRepository.findByIdAndPatientId(id, patient.getId())
                .orElseThrow(() -> new ResourceNotFoundException("MedicalRecord", "id", id));

        return MedicalRecordResponse.from(record);
    }

    @Transactional
    public MedicalRecordResponse updateMyRecord(UserPrincipal currentUser, Long id, MedicalRecordRequest request) {
        PatientProfile patient = patientProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("PatientProfile", "userId", currentUser.getId()));

        MedicalRecord record = medicalRecordRepository.findByIdAndPatientId(id, patient.getId())
                .orElseThrow(() -> new ResourceNotFoundException("MedicalRecord", "id", id));

        record.setRecordType(request.recordType().trim());
        record.setTitle(request.title().trim());
        record.setDescription(request.description().trim());
        record.setRecordDate(request.recordDate());

        MedicalRecord updated = medicalRecordRepository.save(record);
        auditLogService.log(currentUser.getId(), "MEDICAL_RECORD_UPDATED", "MEDICAL_RECORD", updated.getId().toString(), null);
        return MedicalRecordResponse.from(updated);
    }

    @Transactional
    public void deleteMyRecord(UserPrincipal currentUser, Long id) {
        PatientProfile patient = patientProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("PatientProfile", "userId", currentUser.getId()));

        MedicalRecord record = medicalRecordRepository.findByIdAndPatientId(id, patient.getId())
                .orElseThrow(() -> new ResourceNotFoundException("MedicalRecord", "id", id));

        medicalRecordRepository.delete(record);
        auditLogService.log(currentUser.getId(), "MEDICAL_RECORD_DELETED", "MEDICAL_RECORD", id.toString(), null);
    }

    @Transactional(readOnly = true)
    public PageResponse<MedicalRecordResponse> getPatientRecordsByProfessional(
            UserPrincipal currentUser, Long patientId, Pageable pageable) {

        if (currentUser.getRole() != Role.HEALTHCARE_PROFESSIONAL && currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only healthcare professionals or administrators can access this endpoint");
        }

        if (currentUser.getRole() == Role.HEALTHCARE_PROFESSIONAL) {
            ProfessionalProfile professional = professionalProfileRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProfessionalProfile", "userId", currentUser.getId()));

            boolean hasRelationship = appointmentRepository.existsByProfessionalIdAndPatientIdAndStatusIn(
                    professional.getId(), patientId, VALID_RELATIONSHIP_STATUSES
            );

            if (!hasRelationship) {
                throw new ForbiddenException("You are not authorized to view this patient's medical records. No active consultation relationship.");
            }
        }

        if (!patientProfileRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("PatientProfile", "id", patientId);
        }

        // Auditable access
        auditLogService.log(currentUser.getId(), "MEDICAL_RECORD_ACCESSED", "PATIENT", patientId.toString(), null);

        Page<MedicalRecord> page = medicalRecordRepository.findByPatientIdOrderByRecordDateDescCreatedAtDesc(
                patientId, pageable
        );
        return PageResponse.from(page.map(MedicalRecordResponse::from));
    }
}
