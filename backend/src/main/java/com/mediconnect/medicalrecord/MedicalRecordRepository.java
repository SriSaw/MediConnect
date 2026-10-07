package com.mediconnect.medicalrecord;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    Page<MedicalRecord> findByPatientIdOrderByRecordDateDescCreatedAtDesc(Long patientId, Pageable pageable);

    Optional<MedicalRecord> findByIdAndPatientId(Long id, Long patientId);
}
