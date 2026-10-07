package com.mediconnect.appointment;

import com.mediconnect.appointment.dto.AppointmentResponse;
import com.mediconnect.appointment.dto.CreateAppointmentRequest;
import com.mediconnect.audit.AuditLogService;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.exception.ConflictException;
import com.mediconnect.exception.ForbiddenException;
import com.mediconnect.notification.NotificationService;
import com.mediconnect.patient.PatientProfile;
import com.mediconnect.patient.PatientProfileRepository;
import com.mediconnect.professional.Availability;
import com.mediconnect.professional.AvailabilityRepository;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientProfileRepository patientProfileRepository;

    @Mock
    private ProfessionalProfileRepository professionalProfileRepository;

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private com.mediconnect.appointment.repository.AppointmentReportRepository appointmentReportRepository;

    private AppointmentService appointmentService;

    private User patientUser;
    private PatientProfile patientProfile;
    private User docUser;
    private ProfessionalProfile docProfile;
    private UserPrincipal patientPrincipal;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService(
                appointmentRepository,
                patientProfileRepository,
                professionalProfileRepository,
                availabilityRepository,
                notificationService,
                auditLogService,
                appointmentReportRepository
        );

        patientUser = new User("Alex", "patient@test.local", "hash", null, Role.PATIENT, UserStatus.ACTIVE);
        patientUser.setId(1L);
        patientProfile = new PatientProfile(patientUser);
        patientProfile.setId(10L);
        patientPrincipal = UserPrincipal.create(patientUser);

        docUser = new User("Dr. Smith", "doc@test.local", "hash", null, Role.HEALTHCARE_PROFESSIONAL, UserStatus.ACTIVE);
        docUser.setId(2L);
        docProfile = new ProfessionalProfile(docUser, "Cardiology", "LIC-123", 10, "Bio", BigDecimal.valueOf(100), true);
        docProfile.setId(20L);
    }

    @Test
    @DisplayName("Should successfully book appointment within valid availability")
    void testBookAppointment_Success() {
        LocalDate futureDate = LocalDate.now().plusDays(5);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(10, 30);

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                20L, futureDate, start, end, "Routine checkup"
        );

        when(patientProfileRepository.findByUserId(1L)).thenReturn(Optional.of(patientProfile));
        when(professionalProfileRepository.findByIdWithLock(20L)).thenReturn(Optional.of(docProfile));

        DayOfWeek dow = futureDate.getDayOfWeek();
        Availability schedule = new Availability(docProfile, dow, LocalTime.of(9, 0), LocalTime.of(17, 0), true);
        when(availabilityRepository.findCoveringAvailability(eq(20L), eq(dow), eq(start), eq(end)))
                .thenReturn(List.of(schedule));

        when(appointmentRepository.findConflictingAppointments(eq(20L), eq(futureDate), eq(start), eq(end), any(), isNull()))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.findPatientConflictingAppointments(eq(10L), eq(futureDate), eq(start), eq(end), any(), isNull()))
                .thenReturn(Collections.emptyList());

        Appointment savedAppointment = new Appointment(patientProfile, docProfile, futureDate, start, end, AppointmentStatus.CONFIRMED, "Routine checkup");
        savedAppointment.setId(100L);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppointment);

        AppointmentResponse response = appointmentService.bookAppointment(patientPrincipal, request);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals(AppointmentStatus.CONFIRMED, response.status());
        verify(notificationService, times(2)).createNotification(any(), any(), any(), any());
        verify(auditLogService).log(eq(1L), eq("APPOINTMENT_CREATED"), eq("APPOINTMENT"), eq("100"), isNull());
    }

    @Test
    @DisplayName("Security Test #9: Double booking fails with ConflictException")
    void testBookAppointment_DoubleBooking_Fails() {
        LocalDate futureDate = LocalDate.now().plusDays(5);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(10, 30);

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                20L, futureDate, start, end, "Second appointment"
        );

        when(patientProfileRepository.findByUserId(1L)).thenReturn(Optional.of(patientProfile));
        when(professionalProfileRepository.findByIdWithLock(20L)).thenReturn(Optional.of(docProfile));

        DayOfWeek dow = futureDate.getDayOfWeek();
        Availability schedule = new Availability(docProfile, dow, LocalTime.of(9, 0), LocalTime.of(17, 0), true);
        when(availabilityRepository.findCoveringAvailability(eq(20L), eq(dow), eq(start), eq(end)))
                .thenReturn(List.of(schedule));

        // Existing conflicting appointment for this professional
        Appointment existing = new Appointment(patientProfile, docProfile, futureDate, start, end, AppointmentStatus.CONFIRMED, "Prior booking");
        when(appointmentRepository.findConflictingAppointments(eq(20L), eq(futureDate), eq(start), eq(end), any(), isNull()))
                .thenReturn(List.of(existing));

        assertThrows(ConflictException.class, () -> appointmentService.bookAppointment(patientPrincipal, request));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject booking outside professional available hours")
    void testBookAppointment_OutsideAvailability_Fails() {
        LocalDate futureDate = LocalDate.now().plusDays(5);
        LocalTime start = LocalTime.of(22, 0);
        LocalTime end = LocalTime.of(22, 30);

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                20L, futureDate, start, end, "Late night checkup"
        );

        when(patientProfileRepository.findByUserId(1L)).thenReturn(Optional.of(patientProfile));
        when(professionalProfileRepository.findByIdWithLock(20L)).thenReturn(Optional.of(docProfile));

        DayOfWeek dow = futureDate.getDayOfWeek();
        when(availabilityRepository.findCoveringAvailability(eq(20L), eq(dow), eq(start), eq(end)))
                .thenReturn(Collections.emptyList());

        assertThrows(BadRequestException.class, () -> appointmentService.bookAppointment(patientPrincipal, request));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject booking for unverified professional")
    void testBookAppointment_UnverifiedProfessional_Fails() {
        docProfile.setVerified(false);

        LocalDate futureDate = LocalDate.now().plusDays(5);
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                20L, futureDate, LocalTime.of(10, 0), LocalTime.of(10, 30), "Checkup"
        );

        when(patientProfileRepository.findByUserId(1L)).thenReturn(Optional.of(patientProfile));
        when(professionalProfileRepository.findByIdWithLock(20L)).thenReturn(Optional.of(docProfile));

        assertThrows(BadRequestException.class, () -> appointmentService.bookAppointment(patientPrincipal, request));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should prevent cancelling already completed appointment")
    void testCancelAppointment_Completed_Fails() {
        Appointment appointment = new Appointment(
                patientProfile, docProfile, LocalDate.now().minusDays(1),
                LocalTime.of(10, 0), LocalTime.of(10, 30), AppointmentStatus.COMPLETED, "Checkup"
        );
        appointment.setId(50L);

        when(appointmentRepository.findById(50L)).thenReturn(Optional.of(appointment));

        assertThrows(BadRequestException.class, () -> appointmentService.cancelAppointment(patientPrincipal, 50L, "Too late"));
    }

    @Test
    @DisplayName("Should prevent unauthorized third-party user from cancelling appointment")
    void testCancelAppointment_UnauthorizedUser_Fails() {
        User otherUser = new User("Intruder", "intruder@test.local", "hash", null, Role.PATIENT, UserStatus.ACTIVE);
        otherUser.setId(99L);
        UserPrincipal otherPrincipal = UserPrincipal.create(otherUser);

        Appointment appointment = new Appointment(
                patientProfile, docProfile, LocalDate.now().plusDays(2),
                LocalTime.of(10, 0), LocalTime.of(10, 30), AppointmentStatus.CONFIRMED, "Checkup"
        );
        appointment.setId(50L);

        when(appointmentRepository.findById(50L)).thenReturn(Optional.of(appointment));

        assertThrows(ForbiddenException.class, () -> appointmentService.cancelAppointment(otherPrincipal, 50L, "Malicious cancel"));
    }
}
