package com.mediconnect.medicalrecord;

import com.mediconnect.appointment.Appointment;
import com.mediconnect.appointment.AppointmentRepository;
import com.mediconnect.appointment.AppointmentStatus;
import com.mediconnect.audit.AuditLogService;
import com.mediconnect.exception.ForbiddenException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.patient.PatientProfile;
import com.mediconnect.patient.PatientProfileRepository;
import com.mediconnect.professional.ProfessionalProfile;
import com.mediconnect.professional.ProfessionalProfileRepository;
import com.mediconnect.security.UserPrincipal;
import com.mediconnect.user.Role;
import com.mediconnect.user.User;
import com.mediconnect.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalRecordSecurityTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private PatientProfileRepository patientProfileRepository;

    @Mock
    private ProfessionalProfileRepository professionalProfileRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AuditLogService auditLogService;

    private MedicalRecordService medicalRecordService;

    private User patientUserA;
    private User patientUserB;
    private PatientProfile patientProfileA;
    private PatientProfile patientProfileB;

    private User docUserA;
    private ProfessionalProfile docProfileA;

    @BeforeEach
    void setUp() {
        medicalRecordService = new MedicalRecordService(
                medicalRecordRepository,
                patientProfileRepository,
                professionalProfileRepository,
                appointmentRepository,
                auditLogService
        );

        patientUserA = new User("Alice", "alice@test.local", "hash", null, Role.PATIENT, UserStatus.ACTIVE);
        patientUserA.setId(1L);
        patientProfileA = new PatientProfile(patientUserA);
        patientProfileA.setId(10L);

        patientUserB = new User("Bob", "bob@test.local", "hash", null, Role.PATIENT, UserStatus.ACTIVE);
        patientUserB.setId(2L);
        patientProfileB = new PatientProfile(patientUserB);
        patientProfileB.setId(20L);

        docUserA = new User("Dr. Gregory", "doc@test.local", "hash", null, Role.HEALTHCARE_PROFESSIONAL, UserStatus.ACTIVE);
        docUserA.setId(3L);
        docProfileA = new ProfessionalProfile(docUserA, "General Medicine", "LIC-GM-1", 12, "Bio", null, true);
        docProfileA.setId(30L);
    }

    @Test
    @DisplayName("Security Test #1: Patient A cannot access Patient B's medical record by ID")
    void testPatientACannotAccessPatientBsRecord() {
        UserPrincipal principalA = UserPrincipal.create(patientUserA);

        when(patientProfileRepository.findByUserId(1L)).thenReturn(Optional.of(patientProfileA));
        // Looking up record belonging to patient B (id 20), query searches by patientProfileA id (10)
        when(medicalRecordRepository.findByIdAndPatientId(999L, 10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                medicalRecordService.getMyRecordById(principalA, 999L));
    }

    @Test
    @DisplayName("Doctor without consultation relationship cannot access Patient records")
    void testDoctorWithoutRelationshipCannotAccessPatientRecords() {
        UserPrincipal docPrincipal = UserPrincipal.create(docUserA);

        when(professionalProfileRepository.findByUserId(3L)).thenReturn(Optional.of(docProfileA));
        when(appointmentRepository.existsByProfessionalIdAndPatientIdAndStatusIn(eq(30L), eq(10L), any()))
                .thenReturn(false);

        Pageable pageable = PageRequest.of(0, 10);
        assertThrows(ForbiddenException.class, () ->
                medicalRecordService.getPatientRecordsByProfessional(docPrincipal, 10L, pageable));
        verify(medicalRecordRepository, never()).findByPatientIdOrderByRecordDateDescCreatedAtDesc(any(), any());
    }

    @Test
    @DisplayName("Doctor with active consultation relationship CAN access Patient records")
    void testDoctorWithRelationshipCanAccessPatientRecords() {
        UserPrincipal docPrincipal = UserPrincipal.create(docUserA);

        when(professionalProfileRepository.findByUserId(3L)).thenReturn(Optional.of(docProfileA));
        when(appointmentRepository.existsByProfessionalIdAndPatientIdAndStatusIn(eq(30L), eq(10L), any()))
                .thenReturn(true);
        when(patientProfileRepository.existsById(10L)).thenReturn(true);

        MedicalRecord record = new MedicalRecord(patientProfileA, "Diagnosis", "Hypertension", "Mild BP elevation", LocalDate.now());
        record.setId(100L);
        Pageable pageable = PageRequest.of(0, 10);
        when(medicalRecordRepository.findByPatientIdOrderByRecordDateDescCreatedAtDesc(eq(10L), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(record)));

        var response = medicalRecordService.getPatientRecordsByProfessional(docPrincipal, 10L, pageable);

        assertNotNull(response);
        assertEquals(1, response.content().size());
        assertEquals("Hypertension", response.content().get(0).title());
        verify(auditLogService).log(eq(3L), eq("MEDICAL_RECORD_ACCESSED"), eq("PATIENT"), eq("10"), isNull());
    }
}
