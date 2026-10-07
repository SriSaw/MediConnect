package com.mediconnect.appointment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {

    Page<Appointment> findByPatientIdOrderByAppointmentDateDescStartTimeDesc(Long patientId, Pageable pageable);

    Page<Appointment> findByProfessionalIdOrderByAppointmentDateDescStartTimeDesc(Long professionalId, Pageable pageable);

    @Query("SELECT a FROM Appointment a WHERE a.professional.id = :professionalId " +
           "AND a.appointmentDate = :date " +
           "AND a.status IN :activeStatuses " +
           "AND (:excludeId IS NULL OR a.id != :excludeId) " +
           "AND NOT (a.endTime <= :startTime OR a.startTime >= :endTime)")
    List<Appointment> findConflictingAppointments(
            @Param("professionalId") Long professionalId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("activeStatuses") Collection<AppointmentStatus> activeStatuses,
            @Param("excludeId") Long excludeId
    );

    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId " +
           "AND a.appointmentDate = :date " +
           "AND a.status IN :activeStatuses " +
           "AND (:excludeId IS NULL OR a.id != :excludeId) " +
           "AND NOT (a.endTime <= :startTime OR a.startTime >= :endTime)")
    List<Appointment> findPatientConflictingAppointments(
            @Param("patientId") Long patientId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("activeStatuses") Collection<AppointmentStatus> activeStatuses,
            @Param("excludeId") Long excludeId
    );

    boolean existsByProfessionalIdAndPatientIdAndStatusIn(
            Long professionalId, Long patientId, Collection<AppointmentStatus> statuses);

    long countByStatus(AppointmentStatus status);

    long countByAppointmentDate(LocalDate date);

    @Query("SELECT a FROM Appointment a " +
           "WHERE (:status IS NULL OR a.status = :status) " +
           "AND (:date IS NULL OR a.appointmentDate = :date) " +
           "ORDER BY a.appointmentDate DESC, a.startTime DESC")
    Page<Appointment> findAllWithFilters(
            @Param("status") AppointmentStatus status,
            @Param("date") LocalDate date,
            Pageable pageable
    );
}
