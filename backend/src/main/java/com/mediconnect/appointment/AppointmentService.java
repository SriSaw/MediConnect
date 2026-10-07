package com.mediconnect.appointment;

import com.mediconnect.appointment.dto.AppointmentResponse;
import com.mediconnect.appointment.dto.CreateAppointmentRequest;
import com.mediconnect.audit.AuditLogService;
import com.mediconnect.common.PageResponse;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.exception.ConflictException;
import com.mediconnect.exception.ForbiddenException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.notification.NotificationService;
import com.mediconnect.notification.NotificationType;
import com.mediconnect.patient.PatientProfile;
import com.mediconnect.patient.PatientProfileRepository;
import com.mediconnect.professional.Availability;
import com.mediconnect.professional.AvailabilityRepository;
import com.mediconnect.professional.ProfessionalProfile;
import com.mediconnect.professional.ProfessionalProfileRepository;
import com.mediconnect.security.UserPrincipal;
import com.mediconnect.user.Role;
import com.mediconnect.user.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private static final Set<AppointmentStatus> ACTIVE_STATUSES = Set.of(
            AppointmentStatus.PENDING,
            AppointmentStatus.CONFIRMED,
            AppointmentStatus.COMPLETED
    );

    private final AppointmentRepository appointmentRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;
    private final AvailabilityRepository availabilityRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            PatientProfileRepository patientProfileRepository,
            ProfessionalProfileRepository professionalProfileRepository,
            AvailabilityRepository availabilityRepository,
            NotificationService notificationService,
            AuditLogService auditLogService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.professionalProfileRepository = professionalProfileRepository;
        this.availabilityRepository = availabilityRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public AppointmentResponse bookAppointment(UserPrincipal currentUser, CreateAppointmentRequest request) {
        // 1. Resolve patient
        PatientProfile patient = patientProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("PatientProfile", "userId", currentUser.getId()));

        // 2. Lock and verify professional (pessimistic write lock ensures serialized slot validation per professional)
        ProfessionalProfile professional = professionalProfileRepository.findByIdWithLock(request.professionalId())
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalProfile", "id", request.professionalId()));

        if (professional.getUser().getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("Healthcare professional is not active");
        }

        if (!professional.isVerified()) {
            throw new BadRequestException("Cannot book appointments with an unverified healthcare professional");
        }

        // 3. Validate times and dates
        if (!request.startTime().isBefore(request.endTime())) {
            throw new BadRequestException("Appointment start time must be before end time");
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (request.appointmentDate().isBefore(today)) {
            throw new BadRequestException("Appointment date cannot be in the past");
        }

        if (request.appointmentDate().isEqual(today) && request.startTime().isBefore(now)) {
            throw new BadRequestException("Appointment time cannot be in the past");
        }

        // 4. Verify selected slot falls within professional availability schedule
        DayOfWeek dayOfWeek = request.appointmentDate().getDayOfWeek();
        List<Availability> coveringSchedules = availabilityRepository.findCoveringAvailability(
                professional.getId(), dayOfWeek, request.startTime(), request.endTime()
        );

        if (coveringSchedules.isEmpty()) {
            throw new BadRequestException(String.format(
                    "Selected time %s - %s is not within the healthcare professional's available hours on %s",
                    request.startTime(), request.endTime(), dayOfWeek));
        }

        // 5. Prevent double booking - Check professional conflicting appointments
        List<Appointment> profConflicts = appointmentRepository.findConflictingAppointments(
                professional.getId(),
                request.appointmentDate(),
                request.startTime(),
                request.endTime(),
                ACTIVE_STATUSES,
                null
        );

        if (!profConflicts.isEmpty()) {
            log.warn("Double booking rejected for professional {} on {} at {}-{}",
                    professional.getId(), request.appointmentDate(), request.startTime(), request.endTime());
            throw new ConflictException("The selected time slot is already booked for this healthcare professional. Please choose another time.");
        }

        // 6. Check patient conflicting appointments
        List<Appointment> patientConflicts = appointmentRepository.findPatientConflictingAppointments(
                patient.getId(),
                request.appointmentDate(),
                request.startTime(),
                request.endTime(),
                ACTIVE_STATUSES,
                null
        );

        if (!patientConflicts.isEmpty()) {
            throw new ConflictException("You already have an appointment scheduled during this time window.");
        }

        // 7. Save appointment
        Appointment appointment = new Appointment(
                patient,
                professional,
                request.appointmentDate(),
                request.startTime(),
                request.endTime(),
                AppointmentStatus.CONFIRMED,
                request.reason().trim()
        );

        Appointment saved = appointmentRepository.save(appointment);

        // 8. Notifications
        notificationService.createNotification(
                professional.getUser(),
                "New Appointment Booked",
                String.format("Patient %s has booked an appointment for %s from %s to %s. Reason: %s",
                        patient.getUser().getName(), saved.getAppointmentDate(), saved.getStartTime(), saved.getEndTime(), saved.getReason()),
                NotificationType.APPOINTMENT_BOOKED
        );

        notificationService.createNotification(
                patient.getUser(),
                "Appointment Confirmed",
                String.format("Your consultation with Dr. %s is confirmed for %s at %s.",
                        professional.getUser().getName(), saved.getAppointmentDate(), saved.getStartTime()),
                NotificationType.APPOINTMENT_CONFIRMED
        );

        // 9. Audit
        auditLogService.log(currentUser.getId(), "APPOINTMENT_CREATED", "APPOINTMENT", saved.getId().toString(), null);

        return AppointmentResponse.from(saved);
    }

    @Transactional
    public AppointmentResponse cancelAppointment(UserPrincipal currentUser, Long id, String reason) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Appointment is already cancelled");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Completed appointments cannot be cancelled");
        }

        // Authorization check
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isPatient = appointment.getPatient().getUser().getId().equals(currentUser.getId());
        boolean isProfessional = appointment.getProfessional().getUser().getId().equals(currentUser.getId());

        if (!isAdmin && !isPatient && !isProfessional) {
            throw new ForbiddenException("You are not authorized to cancel this appointment");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment updated = appointmentRepository.save(appointment);

        String cancellationDetail = (reason != null && !reason.isBlank()) ? " Reason: " + reason.trim() : "";

        // Notify other party
        if (isPatient) {
            notificationService.createNotification(
                    appointment.getProfessional().getUser(),
                    "Appointment Cancelled",
                    String.format("Patient %s cancelled the appointment scheduled for %s at %s.%s",
                            appointment.getPatient().getUser().getName(), appointment.getAppointmentDate(), appointment.getStartTime(), cancellationDetail),
                    NotificationType.APPOINTMENT_CANCELLED
            );
        } else if (isProfessional) {
            notificationService.createNotification(
                    appointment.getPatient().getUser(),
                    "Appointment Cancelled",
                    String.format("Dr. %s had to cancel your appointment scheduled for %s at %s.%s",
                            appointment.getProfessional().getUser().getName(), appointment.getAppointmentDate(), appointment.getStartTime(), cancellationDetail),
                    NotificationType.APPOINTMENT_CANCELLED
            );
        } else if (isAdmin) {
            notificationService.createNotification(
                    appointment.getPatient().getUser(),
                    "Appointment Cancelled by Admin",
                    String.format("Your appointment on %s has been cancelled by administration.%s",
                            appointment.getAppointmentDate(), cancellationDetail),
                    NotificationType.APPOINTMENT_CANCELLED
            );
            notificationService.createNotification(
                    appointment.getProfessional().getUser(),
                    "Appointment Cancelled by Admin",
                    String.format("Appointment with %s on %s was cancelled by administration.%s",
                            appointment.getPatient().getUser().getName(), appointment.getAppointmentDate(), cancellationDetail),
                    NotificationType.APPOINTMENT_CANCELLED
            );
        }

        auditLogService.log(currentUser.getId(), "APPOINTMENT_CANCELLED", "APPOINTMENT", id.toString(), null);

        return AppointmentResponse.from(updated);
    }

    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponse> getPatientAppointments(UserPrincipal currentUser, Pageable pageable) {
        PatientProfile patient = patientProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("PatientProfile", "userId", currentUser.getId()));

        Page<Appointment> page = appointmentRepository.findByPatientIdOrderByAppointmentDateDescStartTimeDesc(
                patient.getId(), pageable
        );
        return PageResponse.from(page.map(AppointmentResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponse> getProfessionalAppointments(UserPrincipal currentUser, Pageable pageable) {
        ProfessionalProfile professional = professionalProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalProfile", "userId", currentUser.getId()));

        Page<Appointment> page = appointmentRepository.findByProfessionalIdOrderByAppointmentDateDescStartTimeDesc(
                professional.getId(), pageable
        );
        return PageResponse.from(page.map(AppointmentResponse::from));
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(UserPrincipal currentUser, Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isPatient = appointment.getPatient().getUser().getId().equals(currentUser.getId());
        boolean isProfessional = appointment.getProfessional().getUser().getId().equals(currentUser.getId());

        if (!isAdmin && !isPatient && !isProfessional) {
            throw new ForbiddenException("You are not authorized to view this appointment");
        }

        return AppointmentResponse.from(appointment);
    }

    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponse> getAllAppointments(AppointmentStatus status, LocalDate date, Pageable pageable) {
        Page<Appointment> page = appointmentRepository.findAllWithFilters(status, date, pageable);
        return PageResponse.from(page.map(AppointmentResponse::from));
    }
}
