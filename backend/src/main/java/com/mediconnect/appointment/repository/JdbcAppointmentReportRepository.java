package com.mediconnect.appointment.repository;

import com.mediconnect.appointment.AppointmentStatus;
import com.mediconnect.appointment.dto.AppointmentReportResponse;
import com.mediconnect.common.PageResponse;
import com.mediconnect.exception.DatabaseOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of AppointmentReportRepository.
 * Provides explicit evidence of:
 * - DataSource direct connection acquisition
 * - Low-level JDBC Transaction Management (setAutoCommit, commit, rollback)
 * - Parameterized PreparedStatements preventing SQL injection
 * - Multi-table SQL JOIN queries extracting denormalized reports
 * - Type-safe RowMapper ResultSet conversion
 * - Comprehensive SQLException and DataAccessException handling
 */
@Repository
public class JdbcAppointmentReportRepository implements AppointmentReportRepository {

    private static final Logger log = LoggerFactory.getLogger(JdbcAppointmentReportRepository.class);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public JdbcAppointmentReportRepository(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    /**
     * Executes parameterized dynamic SQL with multi-table JOINs across appointments,
     * patients, professionals, and users.
     */
    @Override
    public PageResponse<AppointmentReportResponse> searchAppointments(
            AppointmentStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Long professionalId,
            Long patientId,
            Pageable pageable
    ) {
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (status != null) {
            whereClause.append(" AND a.status = ? ");
            params.add(status.name());
        }

        if (startDate != null) {
            whereClause.append(" AND a.appointment_date >= ? ");
            params.add(Date.valueOf(startDate));
        }

        if (endDate != null) {
            whereClause.append(" AND a.appointment_date <= ? ");
            params.add(Date.valueOf(endDate));
        }

        if (professionalId != null) {
            whereClause.append(" AND a.professional_id = ? ");
            params.add(professionalId);
        }

        if (patientId != null) {
            whereClause.append(" AND a.patient_id = ? ");
            params.add(patientId);
        }

        String countSql = """
            SELECT COUNT(*)
            FROM appointments a
        """ + whereClause;

        try {
            Long totalNullable = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());
            long total = totalNullable != null ? totalNullable : 0L;

            int limit = pageable.getPageSize();
            long offset = pageable.getOffset();

            String querySql = """
                SELECT
                    a.id AS appt_id,
                    a.patient_id,
                    u_pat.name AS patient_name,
                    u_pat.email AS patient_email,
                    a.professional_id,
                    u_prof.name AS professional_name,
                    prof.specialization,
                    prof.consultation_fee,
                    a.appointment_date,
                    a.start_time,
                    a.end_time,
                    a.status,
                    a.reason,
                    a.created_at
                FROM appointments a
                JOIN patient_profiles pat ON a.patient_id = pat.id
                JOIN users u_pat ON pat.user_id = u_pat.id
                JOIN professional_profiles prof ON a.professional_id = prof.id
                JOIN users u_prof ON prof.user_id = u_prof.id
            """ + whereClause + " ORDER BY a.appointment_date DESC, a.start_time DESC LIMIT ? OFFSET ?";

            List<Object> queryParams = new ArrayList<>(params);
            queryParams.add(limit);
            queryParams.add(offset);

            List<AppointmentReportResponse> content = jdbcTemplate.query(
                    querySql,
                    new AppointmentReportRowMapper(),
                    queryParams.toArray()
            );

            int totalPages = (int) Math.ceil((double) total / limit);
            boolean last = (pageable.getPageNumber() + 1) >= totalPages || totalPages == 0;

            return new PageResponse<>(
                    content,
                    pageable.getPageNumber(),
                    limit,
                    total,
                    totalPages,
                    last
            );
        } catch (DataAccessException e) {
            log.error("Failed to execute JDBC appointment search query: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Error searching appointments via JDBC", e);
        }
    }

    /**
     * Demonstrates explicit low-level JDBC transaction management.
     * Manages a raw java.sql.Connection with explicit transaction boundaries:
     * 1. conn.setAutoCommit(false) -> Starts transaction
     * 2. Executes batch status update statement
     * 3. Inserts corresponding audit records in the same atomic transaction
     * 4. conn.commit() -> Commits upon successful execution of all statements
     * 5. conn.rollback() -> Rolls back if any SQLException occurs, preserving integrity
     */
    @Override
    public int batchUpdateStatusInTransaction(List<Long> appointmentIds, AppointmentStatus targetStatus, Long actorUserId) {
        if (appointmentIds == null || appointmentIds.isEmpty()) {
            return 0;
        }

        String updateSql = "UPDATE appointments SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        String auditSql = "INSERT INTO audit_logs (actor_user_id, action, resource_type, resource_id, timestamp) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            // 1. Disable auto-commit to begin manual transaction boundary
            conn.setAutoCommit(false);

            int totalUpdated = 0;

            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                 PreparedStatement auditStmt = conn.prepareStatement(auditSql)) {

                for (Long apptId : appointmentIds) {
                    // Parameterized appointment status update
                    updateStmt.setString(1, targetStatus.name());
                    updateStmt.setLong(2, apptId);
                    updateStmt.addBatch();

                    // Parameterized audit entry within the same atomic transaction
                    if (actorUserId != null) {
                        auditStmt.setLong(1, actorUserId);
                    } else {
                        auditStmt.setNull(1, Types.BIGINT);
                    }
                    auditStmt.setString(2, "BATCH_STATUS_UPDATE");
                    auditStmt.setString(3, "APPOINTMENT");
                    auditStmt.setString(4, String.valueOf(apptId));
                    auditStmt.addBatch();
                }

                int[] updateCounts = updateStmt.executeBatch();
                auditStmt.executeBatch();

                for (int count : updateCounts) {
                    if (count >= 0) {
                        totalUpdated += count;
                    }
                }

                // 2. Commit transaction atomically
                conn.commit();
                log.info("Successfully executed JDBC batch status update for {} appointments in transaction", totalUpdated);
                return totalUpdated;

            } catch (SQLException batchEx) {
                // 3. Rollback on failure to maintain ACID guarantees
                log.warn("Batch update failed, executing JDBC rollback: {}", batchEx.getMessage());
                if (conn != null) {
                    conn.rollback();
                }
                throw batchEx;
            }

        } catch (SQLException e) {
            log.error("JDBC transaction error during batch update: {}", e.getMessage(), e);
            throw new DatabaseOperationException("JDBC transaction failed during batch status update", e);
        } finally {
            if (conn != null) {
                try {
                    // Reset auto-commit and close connection back to Hikari pool
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    log.error("Failed to reset connection auto-commit or close connection: {}", closeEx.getMessage());
                }
            }
        }
    }

    /**
     * RowMapper mapping joined ResultSet columns to AppointmentReportResponse DTO.
     */
    private static class AppointmentReportRowMapper implements RowMapper<AppointmentReportResponse> {
        @Override
        public AppointmentReportResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            Date apptDate = rs.getDate("appointment_date");
            Time startTime = rs.getTime("start_time");
            Time endTime = rs.getTime("end_time");
            Timestamp createdAt = rs.getTimestamp("created_at");

            LocalDate localDate = apptDate != null ? apptDate.toLocalDate() : null;
            LocalTime localStart = startTime != null ? startTime.toLocalTime() : null;
            LocalTime localEnd = endTime != null ? endTime.toLocalTime() : null;
            LocalDateTime localCreatedAt = createdAt != null ? createdAt.toLocalDateTime() : null;

            return new AppointmentReportResponse(
                    rs.getLong("appt_id"),
                    rs.getLong("patient_id"),
                    rs.getString("patient_name"),
                    rs.getString("patient_email"),
                    rs.getLong("professional_id"),
                    rs.getString("professional_name"),
                    rs.getString("specialization"),
                    localDate,
                    localStart,
                    localEnd,
                    AppointmentStatus.valueOf(rs.getString("status")),
                    rs.getString("reason"),
                    rs.getBigDecimal("consultation_fee"),
                    localCreatedAt
            );
        }
    }
}
