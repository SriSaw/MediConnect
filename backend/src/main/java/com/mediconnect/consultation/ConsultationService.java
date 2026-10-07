package com.mediconnect.consultation;

import com.mediconnect.appointment.Appointment;
import com.mediconnect.appointment.AppointmentRepository;
import com.mediconnect.appointment.AppointmentStatus;
import com.mediconnect.audit.AuditLogService;
import com.mediconnect.common.PageResponse;
import com.mediconnect.consultation.dto.ConsultationResponse;
import com.mediconnect.consultation.dto.CreateConsultationRequest;
import com.mediconnect.consultation.dto.UpdateConsultationRequest;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.exception.ConflictException;
import com.mediconnect.exception.ForbiddenException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.notification.NotificationService;
import com.mediconnect.notification.NotificationType;
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

@Service
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final AppointmentRepository appointmentRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public ConsultationService(
            ConsultationRepository consultationRepository,
            AppointmentRepository appointmentRepository,
            ProfessionalProfileRepository professionalProfileRepository,
            PatientProfileRepository patientProfileRepository,
            NotificationService notificationService,
            AuditLogService auditLogService
    ) {
        this.consultationRepository = consultationRepository;
        this.appointmentRepository = appointmentRepository;
        this.professionalProfileRepository = professionalProfileRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public ConsultationResponse createConsultation(UserPrincipal currentUser, CreateConsultationRequest request) {
        if (currentUser.getRole() != Role.HEALTHCARE_PROFESSIONAL) {
            throw new ForbiddenException("Only healthcare professionals can create consultations");
        }

        Appointment appointment = appointmentRepository.findById(request.appointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", request.appointmentId()));

        // Verify professional ownership
        if (!appointment.getProfessional().getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You cannot provide consultation for another professional's appointment");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Cannot create a consultation for a cancelled appointment");
        }

        if (consultationRepository.existsByAppointmentId(appointment.getId())) {
            throw new ConflictException("A consultation already exists for this appointment");
        }

        Consultation consultation = new Consultation(
                appointment,
                appointment.getPatient(),
                appointment.getProfessional(),
                request.notes() != null ? request.notes().trim() : null,
                request.medicalAdvice().trim(),
                request.followUpDate()
        );

        Consultation saved = consultationRepository.save(consultation);

        // Update appointment status to COMPLETED
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        // Notify patient
        notificationService.createNotification(
                appointment.getPatient().getUser(),
                "Consultation Completed",
                String.format("Dr. %s has completed your consultation and recorded medical advice.",
                        appointment.getProfessional().getUser().getName()),
                NotificationType.CONSULTATION_COMPLETED
        );

        auditLogService.log(currentUser.getId(), "CONSULTATION_CREATED", "CONSULTATION", saved.getId().toString(), null);

        return ConsultationResponse.from(saved);
    }

    @Transactional
    public ConsultationResponse updateConsultation(UserPrincipal currentUser, Long id, UpdateConsultationRequest request) {
        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation", "id", id));

        // Patients cannot modify medical advice or consultations
        if (currentUser.getRole() == Role.PATIENT) {
            throw new ForbiddenException("Patients cannot modify medical advice or consultations");
        }

        // Only the assigned professional can update
        if (!consultation.getProfessional().getUser().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only the assigned healthcare professional can update this consultation");
        }

        if (request.notes() != null) {
            consultation.setNotes(request.notes().trim());
        }
        consultation.setMedicalAdvice(request.medicalAdvice().trim());
        consultation.setFollowUpDate(request.followUpDate());

        Consultation updated = consultationRepository.save(consultation);

        // Notify patient
        notificationService.createNotification(
                consultation.getPatient().getUser(),
                "Medical Advice Updated",
                String.format("Dr. %s updated the medical advice for your consultation.",
                        consultation.getProfessional().getUser().getName()),
                NotificationType.NEW_MEDICAL_ADVICE
        );

        return ConsultationResponse.from(updated);
    }

    @Transactional(readOnly = true)
    public ConsultationResponse getConsultationById(UserPrincipal currentUser, Long id) {
        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation", "id", id));

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isPatient = consultation.getPatient().getUser().getId().equals(currentUser.getId());
        boolean isProfessional = consultation.getProfessional().getUser().getId().equals(currentUser.getId());

        if (!isAdmin && !isPatient && !isProfessional) {
            throw new ForbiddenException("You are not authorized to view this consultation");
        }

        return ConsultationResponse.from(consultation);
    }

    @Transactional(readOnly = true)
    public ConsultationResponse getConsultationByAppointmentId(UserPrincipal currentUser, Long appointmentId) {
        Consultation consultation = consultationRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation", "appointmentId", appointmentId));

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isPatient = consultation.getPatient().getUser().getId().equals(currentUser.getId());
        boolean isProfessional = consultation.getProfessional().getUser().getId().equals(currentUser.getId());

        if (!isAdmin && !isPatient && !isProfessional) {
            throw new ForbiddenException("You are not authorized to view this consultation");
        }

        return ConsultationResponse.from(consultation);
    }

    @Transactional(readOnly = true)
    public PageResponse<ConsultationResponse> getMyConsultations(UserPrincipal currentUser, Pageable pageable) {
        if (currentUser.getRole() == Role.PATIENT) {
            PatientProfile patient = patientProfileRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("PatientProfile", "userId", currentUser.getId()));
            Page<Consultation> page = consultationRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId(), pageable);
            return PageResponse.from(page.map(ConsultationResponse::from));
        } else if (currentUser.getRole() == Role.HEALTHCARE_PROFESSIONAL) {
            ProfessionalProfile professional = professionalProfileRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProfessionalProfile", "userId", currentUser.getId()));
            Page<Consultation> page = consultationRepository.findByProfessionalIdOrderByCreatedAtDesc(professional.getId(), pageable);
            return PageResponse.from(page.map(ConsultationResponse::from));
        } else {
            Page<Consultation> page = consultationRepository.findAll(pageable);
            return PageResponse.from(page.map(ConsultationResponse::from));
        }
    }
}
