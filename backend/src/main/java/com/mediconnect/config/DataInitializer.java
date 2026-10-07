package com.mediconnect.config;

import com.mediconnect.appointment.Appointment;
import com.mediconnect.appointment.AppointmentRepository;
import com.mediconnect.appointment.AppointmentStatus;
import com.mediconnect.audit.AuditLog;
import com.mediconnect.audit.AuditLogRepository;
import com.mediconnect.consultation.Consultation;
import com.mediconnect.consultation.ConsultationRepository;
import com.mediconnect.medicalrecord.MedicalRecord;
import com.mediconnect.medicalrecord.MedicalRecordRepository;
import com.mediconnect.patient.PatientProfile;
import com.mediconnect.patient.PatientProfileRepository;
import com.mediconnect.professional.Availability;
import com.mediconnect.professional.AvailabilityRepository;
import com.mediconnect.professional.ProfessionalProfile;
import com.mediconnect.professional.ProfessionalProfileRepository;
import com.mediconnect.settings.SystemSetting;
import com.mediconnect.settings.SystemSettingRepository;
import com.mediconnect.user.Role;
import com.mediconnect.user.User;
import com.mediconnect.user.UserRepository;
import com.mediconnect.user.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String DEFAULT_PASSWORD = "Password123!";

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;
    private final AvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;
    private final ConsultationRepository consultationRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            PatientProfileRepository patientProfileRepository,
            ProfessionalProfileRepository professionalProfileRepository,
            AvailabilityRepository availabilityRepository,
            AppointmentRepository appointmentRepository,
            ConsultationRepository consultationRepository,
            MedicalRecordRepository medicalRecordRepository,
            SystemSettingRepository systemSettingRepository,
            AuditLogRepository auditLogRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.professionalProfileRepository = professionalProfileRepository;
        this.availabilityRepository = availabilityRepository;
        this.appointmentRepository = appointmentRepository;
        this.consultationRepository = consultationRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        initSystemSettings();

        if (userRepository.existsByEmail("admin@mediconnect.local")) {
            log.info("Demo seed data already exists. Skipping data initialization.");
            return;
        }

        log.info("Seeding demo healthcare data for MediConnect prototype...");

        String encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);

        // 1. Admin
        User admin = new User("System Administrator", "admin@mediconnect.local", encodedPassword, "+1-555-0100", Role.ADMIN, UserStatus.ACTIVE);
        admin = userRepository.save(admin);

        // 2. Healthcare Professional 1 (Cardiologist)
        User doctorUser1 = new User("Dr. Sarah Jenkins", "doctor1@mediconnect.local", encodedPassword, "+1-555-0101", Role.HEALTHCARE_PROFESSIONAL, UserStatus.ACTIVE);
        doctorUser1 = userRepository.save(doctorUser1);

        ProfessionalProfile doctorProfile1 = new ProfessionalProfile(
                doctorUser1,
                "Cardiology",
                "MD-CARDIO-88219",
                12,
                "Board-certified Cardiologist specializing in preventive cardiology and hypertension management.",
                BigDecimal.valueOf(120.00),
                true
        );
        doctorProfile1 = professionalProfileRepository.save(doctorProfile1);

        // 3. Healthcare Professional 2 (Dermatologist)
        User doctorUser2 = new User("Dr. Marcus Chen", "doctor2@mediconnect.local", encodedPassword, "+1-555-0102", Role.HEALTHCARE_PROFESSIONAL, UserStatus.ACTIVE);
        doctorUser2 = userRepository.save(doctorUser2);

        ProfessionalProfile doctorProfile2 = new ProfessionalProfile(
                doctorUser2,
                "Dermatology",
                "MD-DERM-44102",
                8,
                "Consultant Dermatologist focusing on adult skin conditions, eczema, and teledermatology reviews.",
                BigDecimal.valueOf(95.00),
                true
        );
        doctorProfile2 = professionalProfileRepository.save(doctorProfile2);

        // 4. Patient 1
        User patientUser1 = new User("Alex Morgan", "patient1@mediconnect.local", encodedPassword, "+1-555-0201", Role.PATIENT, UserStatus.ACTIVE);
        patientUser1 = userRepository.save(patientUser1);

        PatientProfile patientProfile1 = new PatientProfile(patientUser1);
        patientProfile1.setDateOfBirth(LocalDate.of(1992, 5, 14));
        patientProfile1.setGender("FEMALE");
        patientProfile1.setBloodGroup("O+");
        patientProfile1.setAddress("742 Evergreen Terrace, Springfield");
        patientProfile1.setEmergencyContact("Pat Morgan (+1-555-0299)");
        patientProfile1 = patientProfileRepository.save(patientProfile1);

        // 5. Patient 2
        User patientUser2 = new User("David Kim", "patient2@mediconnect.local", encodedPassword, "+1-555-0202", Role.PATIENT, UserStatus.ACTIVE);
        patientUser2 = userRepository.save(patientUser2);

        PatientProfile patientProfile2 = new PatientProfile(patientUser2);
        patientProfile2.setDateOfBirth(LocalDate.of(1985, 11, 23));
        patientProfile2.setGender("MALE");
        patientProfile2.setBloodGroup("A+");
        patientProfile2.setAddress("123 Baker Street, Metropolis");
        patientProfile2.setEmergencyContact("Elena Kim (+1-555-0298)");
        patientProfile2 = patientProfileRepository.save(patientProfile2);

        // 6. Availabilities for Doctor 1 (Monday to Friday, 09:00 - 13:00 and 14:00 - 18:00)
        for (DayOfWeek day : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
            availabilityRepository.save(new Availability(doctorProfile1, day, LocalTime.of(9, 0), LocalTime.of(13, 0), true));
            availabilityRepository.save(new Availability(doctorProfile1, day, LocalTime.of(14, 0), LocalTime.of(18, 0), true));
        }

        // Availabilities for Doctor 2 (Monday, Wednesday, Friday, 10:00 - 16:00)
        for (DayOfWeek day : List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)) {
            availabilityRepository.save(new Availability(doctorProfile2, day, LocalTime.of(10, 0), LocalTime.of(16, 0), true));
        }

        // 7. Sample Medical Records for Patient 1
        medicalRecordRepository.save(new MedicalRecord(
                patientProfile1,
                "DIAGNOSIS",
                "Mild Hypertension Follow-up",
                "Routine checkup indicated blood pressure reading of 135/85 mmHg. Non-critical, lifestyle modification advised.",
                LocalDate.now().minusDays(30)
        ));
        medicalRecordRepository.save(new MedicalRecord(
                patientProfile1,
                "LAB_RESULT",
                "Annual Blood Chemistry Panel",
                "Lipid profile: Total Cholesterol 185 mg/dL, HDL 55 mg/dL, Triglycerides 120 mg/dL. Within target range.",
                LocalDate.now().minusDays(60)
        ));

        // 8. Completed Sample Appointment & Consultation
        Appointment pastAppointment = new Appointment(
                patientProfile1,
                doctorProfile1,
                LocalDate.now().minusDays(5),
                LocalTime.of(10, 0),
                LocalTime.of(10, 30),
                AppointmentStatus.COMPLETED,
                "Annual cardiac screening and blood pressure discussion."
        );
        pastAppointment = appointmentRepository.save(pastAppointment);

        Consultation sampleConsultation = new Consultation(
                pastAppointment,
                patientProfile1,
                doctorProfile1,
                "Patient is asymptomatic. Heart sounds normal (S1/S2 present, no murmurs). Pulse 72 bpm regular.",
                "Continue 30 minutes of moderate aerobic exercise daily. Reduce dietary sodium. Maintain blood pressure log. Follow-up in 6 months.",
                LocalDate.now().plusMonths(6)
        );
        consultationRepository.save(sampleConsultation);

        // 9. Upcoming Sample Appointment
        // Choose next day that is a weekday
        LocalDate upcomingDate = LocalDate.now().plusDays(2);
        while (upcomingDate.getDayOfWeek() == DayOfWeek.SATURDAY || upcomingDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            upcomingDate = upcomingDate.plusDays(1);
        }

        Appointment upcomingAppointment = new Appointment(
                patientProfile1,
                doctorProfile1,
                upcomingDate,
                LocalTime.of(11, 0),
                LocalTime.of(11, 30),
                AppointmentStatus.CONFIRMED,
                "Follow-up check on exercise plan and lifestyle log."
        );
        appointmentRepository.save(upcomingAppointment);

        // 10. Audit Log
        auditLogRepository.save(new AuditLog(admin.getId(), "SYSTEM_INIT", "SYSTEM", "0", "127.0.0.1"));

        log.info("MediConnect seed data initialization completed successfully.");
    }

    private void initSystemSettings() {
        if (!systemSettingRepository.existsByKey("platform_name")) {
            systemSettingRepository.save(new SystemSetting(
                    "platform_name",
                    "MediConnect Academic Prototype",
                    "Public platform display title"
            ));
        }
        if (!systemSettingRepository.existsByKey("appointment_duration_minutes")) {
            systemSettingRepository.save(new SystemSetting(
                    "appointment_duration_minutes",
                    "30",
                    "Default slot length for patient consultations in minutes"
            ));
        }
        if (!systemSettingRepository.existsByKey("max_booking_advance_days")) {
            systemSettingRepository.save(new SystemSetting(
                    "max_booking_advance_days",
                    "60",
                    "Maximum window in advance a patient may reserve an appointment"
            ));
        }
    }
}
