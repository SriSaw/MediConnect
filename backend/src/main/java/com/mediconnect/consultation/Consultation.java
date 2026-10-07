package com.mediconnect.consultation;

import com.mediconnect.appointment.Appointment;
import com.mediconnect.patient.PatientProfile;
import com.mediconnect.professional.ProfessionalProfile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultations")
@Getter
@Setter
@NoArgsConstructor
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientProfile patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professional_id", nullable = false)
    private ProfessionalProfile professional;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "medical_advice", nullable = false, columnDefinition = "TEXT")
    private String medicalAdvice;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Consultation(Appointment appointment, PatientProfile patient, ProfessionalProfile professional,
                        String notes, String medicalAdvice, LocalDate followUpDate) {
        this.appointment = appointment;
        this.patient = patient;
        this.professional = professional;
        this.notes = notes;
        this.medicalAdvice = medicalAdvice;
        this.followUpDate = followUpDate;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
