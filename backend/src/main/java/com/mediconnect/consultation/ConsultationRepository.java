package com.mediconnect.consultation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    Optional<Consultation> findByAppointmentId(Long appointmentId);

    boolean existsByAppointmentId(Long appointmentId);

    Page<Consultation> findByPatientIdOrderByCreatedAtDesc(Long patientId, Pageable pageable);

    Page<Consultation> findByProfessionalIdOrderByCreatedAtDesc(Long professionalId, Pageable pageable);
}
