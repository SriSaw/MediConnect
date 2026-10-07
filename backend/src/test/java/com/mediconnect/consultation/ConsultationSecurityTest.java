package com.mediconnect.consultation;

import com.mediconnect.appointment.Appointment;
import com.mediconnect.appointment.AppointmentRepository;
import com.mediconnect.appointment.AppointmentStatus;
import com.mediconnect.audit.AuditLogService;
import com.mediconnect.consultation.dto.ConsultationResponse;
import com.mediconnect.consultation.dto.CreateConsultationRequest;
import com.mediconnect.consultation.dto.UpdateConsultationRequest;
import com.mediconnect.exception.ForbiddenException;
import com.mediconnect.notification.NotificationService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultationSecurityTest {

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ProfessionalProfileRepository professionalProfileRepository;

    @Mock
    private PatientProfileRepository patientProfileRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditLogService auditLogService;

    private ConsultationService consultationService;

    private User doc1User;
    private User doc2User;
    private User patientUser;
    private ProfessionalProfile doc1Profile;
    private ProfessionalProfile doc2Profile;
    private PatientProfile patientProfile;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        consultationService = new ConsultationService(
                consultationRepository,
                appointmentRepository,
                professionalProfileRepository,
                patientProfileRepository,
                notificationService,
                auditLogService
        );

        doc1User = new User("Dr. Alpha", "doc1@test.local", "hash", null, Role.HEALTHCARE_PROFESSIONAL, UserStatus.ACTIVE);
        doc1User.setId(10L);
        doc1Profile = new ProfessionalProfile(doc1User, "Cardiology", "LIC-A", 10, "Bio", BigDecimal.valueOf(100), true);
        doc1Profile.setId(100L);

        doc2User = new User("Dr. Beta", "doc2@test.local", "hash", null, Role.HEALTHCARE_PROFESSIONAL, UserStatus.ACTIVE);
        doc2User.setId(20L);
        doc2Profile = new ProfessionalProfile(doc2User, "Dermatology", "LIC-B", 5, "Bio", BigDecimal.valueOf(80), true);
        doc2Profile.setId(200L);

        patientUser = new User("John", "patient@test.local", "hash", null, Role.PATIENT, UserStatus.ACTIVE);
        patientUser.setId(30L);
        patientProfile = new PatientProfile(patientUser);
        patientProfile.setId(300L);

        appointment = new Appointment(
                patientProfile, doc1Profile, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(10, 30),
                AppointmentStatus.CONFIRMED, "Chest tightness"
        );
        appointment.setId(500L);
    }

    @Test
    @DisplayName("Security Test #3: Professional cannot create consultation for another professional's appointment")
    void testProfessionalCannotCreateConsultationForAnotherProfessionalsAppointment() {
        // Doctor 2 attempts to create a consultation for Doctor 1's appointment
        UserPrincipal doc2Principal = UserPrincipal.create(doc2User);
        CreateConsultationRequest request = new CreateConsultationRequest(
                500L, "Illicit notes", "Prescribe medication X", null
        );

        when(appointmentRepository.findById(500L)).thenReturn(Optional.of(appointment));

        assertThrows(ForbiddenException.class, () ->
                consultationService.createConsultation(doc2Principal, request));
        verify(consultationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Security Test #4: Patient cannot modify consultation medical advice")
    void testPatientCannotModifyConsultationAdvice() {
        UserPrincipal patientPrincipal = UserPrincipal.create(patientUser);

        Consultation consultation = new Consultation(
                appointment, patientProfile, doc1Profile, "Clinical notes", "Original medical advice", null
        );
        consultation.setId(700L);

        when(consultationRepository.findById(700L)).thenReturn(Optional.of(consultation));

        UpdateConsultationRequest updateRequest = new UpdateConsultationRequest(
                "Tampered notes", "Tampered self-prescribed advice", null
        );

        assertThrows(ForbiddenException.class, () ->
                consultationService.updateConsultation(patientPrincipal, 700L, updateRequest));
        verify(consultationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Assigned professional successfully creates consultation and marks appointment COMPLETED")
    void testAssignedProfessionalCreatesConsultation_Success() {
        UserPrincipal doc1Principal = UserPrincipal.create(doc1User);
        CreateConsultationRequest request = new CreateConsultationRequest(
                500L, "Clinical observation notes", "Rest for 3 days and monitor BP", LocalDate.now().plusWeeks(2)
        );

        when(appointmentRepository.findById(500L)).thenReturn(Optional.of(appointment));
        when(consultationRepository.existsByAppointmentId(500L)).thenReturn(false);

        Consultation savedConsultation = new Consultation(
                appointment, patientProfile, doc1Profile, request.notes(), request.medicalAdvice(), request.followUpDate()
        );
        savedConsultation.setId(888L);
        when(consultationRepository.save(any(Consultation.class))).thenReturn(savedConsultation);

        ConsultationResponse response = consultationService.createConsultation(doc1Principal, request);

        assertNotNull(response);
        assertEquals("Rest for 3 days and monitor BP", response.medicalAdvice());
        assertEquals(AppointmentStatus.COMPLETED, appointment.getStatus());
        verify(appointmentRepository).save(appointment);
        verify(notificationService).createNotification(any(), any(), any(), any());
        verify(auditLogService).log(eq(10L), eq("CONSULTATION_CREATED"), eq("CONSULTATION"), eq("888"), isNull());
    }
}
