package com.mediconnect.professional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    List<Availability> findByProfessionalIdOrderByDayOfWeekAscStartTimeAsc(Long professionalId);

    List<Availability> findByProfessionalIdAndDayOfWeek(Long professionalId, DayOfWeek dayOfWeek);

    @Query("SELECT a FROM Availability a WHERE a.professional.id = :professionalId " +
           "AND a.dayOfWeek = :dayOfWeek " +
           "AND a.available = true " +
           "AND a.startTime <= :startTime " +
           "AND a.endTime >= :endTime")
    List<Availability> findCoveringAvailability(
            @Param("professionalId") Long professionalId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Query("SELECT a FROM Availability a WHERE a.professional.id = :professionalId " +
           "AND a.dayOfWeek = :dayOfWeek " +
           "AND (:excludeId IS NULL OR a.id != :excludeId) " +
           "AND NOT (a.endTime <= :startTime OR a.startTime >= :endTime)")
    List<Availability> findOverlappingAvailability(
            @Param("professionalId") Long professionalId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );
}
