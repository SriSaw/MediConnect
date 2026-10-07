package com.mediconnect.appointment.repository;

import com.mediconnect.appointment.AppointmentStatus;
import com.mediconnect.appointment.dto.AppointmentReportResponse;
import com.mediconnect.common.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface abstraction for advanced appointment reporting and batch operations.
 * Demonstrates the separation of concerns between business services and
 * specialized JDBC persistence implementations.
 */
public interface AppointmentReportRepository {

    /**
     * Executes parameterized dynamic SQL queries over PostgreSQL to filter and paginate appointments.
     *
     * @param status         optional status filter
     * @param startDate      optional start date filter
     * @param endDate        optional end date filter
     * @param professionalId optional professional profile ID filter
     * @param patientId      optional patient profile ID filter
     * @param pageable       pagination request
     * @return paginated AppointmentReportResponse
     */
    PageResponse<AppointmentReportResponse> searchAppointments(
            AppointmentStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Long professionalId,
            Long patientId,
            Pageable pageable
    );

    /**
     * Demonstrates an explicit, low-level JDBC transaction.
     * Atomically updates appointment statuses and inserts audit logs within a manual
     * Connection transaction boundary (setAutoCommit, commit, rollback).
     *
     * @param appointmentIds list of appointment IDs to update
     * @param targetStatus   new appointment status
     * @param actorUserId    ID of the user initiating the action
     * @return number of affected rows
     */
    int batchUpdateStatusInTransaction(List<Long> appointmentIds, AppointmentStatus targetStatus, Long actorUserId);
}
