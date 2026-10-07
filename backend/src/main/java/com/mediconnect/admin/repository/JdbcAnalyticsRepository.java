package com.mediconnect.admin.repository;

import com.mediconnect.admin.dto.AnalyticsOverviewResponse;
import com.mediconnect.exception.DatabaseOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * JDBC implementation of AnalyticsRepository utilizing Spring's JdbcTemplate.
 * Demonstrates:
 * - Direct JDBC connectivity backed by PostgreSQL DataSource (HikariCP)
 * - Optimized SQL aggregate subqueries to replace multiple round-trips
 * - RowMapper implementation for type-safe ResultSet extraction
 * - DataAccessException handling and translation into domain DatabaseOperationException
 */
@Repository
public class JdbcAnalyticsRepository implements AnalyticsRepository {

    private static final Logger log = LoggerFactory.getLogger(JdbcAnalyticsRepository.class);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public JdbcAnalyticsRepository(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    /**
     * Executes a single aggregated SQL query over PostgreSQL.
     * Replaces 8 individual JPA count queries with one atomic round-trip,
     * demonstrating efficient SQL aggregate design.
     */
    @Override
    public AnalyticsOverviewResponse getOverview() {
        String sql = """
            SELECT
                (SELECT COUNT(*) FROM users) AS total_users,
                (SELECT COUNT(*) FROM users WHERE role = 'PATIENT') AS total_patients,
                (SELECT COUNT(*) FROM users WHERE role = 'HEALTHCARE_PROFESSIONAL') AS total_professionals,
                (SELECT COUNT(*) FROM users WHERE status = 'ACTIVE') AS active_users,
                (SELECT COUNT(*) FROM appointments) AS total_appointments,
                (SELECT COUNT(*) FROM appointments WHERE status = 'COMPLETED') AS completed_appointments,
                (SELECT COUNT(*) FROM appointments WHERE status = 'CANCELLED') AS cancelled_appointments,
                (SELECT COUNT(*) FROM appointments WHERE status IN ('PENDING', 'CONFIRMED')) AS pending_appointments,
                (SELECT COUNT(*) FROM appointments WHERE appointment_date = CURRENT_DATE) AS today_appointments
        """;

        try {
            return jdbcTemplate.queryForObject(sql, new AnalyticsOverviewRowMapper());
        } catch (DataAccessException e) {
            log.error("Failed to execute JDBC analytics overview query: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Error fetching analytics overview via JDBC", e);
        }
    }

    /**
     * Executes a GROUP BY query returning appointment distribution mapped into a Java Collections Map.
     */
    @Override
    public Map<String, Long> getAppointmentStatusDistribution() {
        String sql = "SELECT status, COUNT(*) AS count_num FROM appointments GROUP BY status";

        try {
            return jdbcTemplate.query(sql, (rs) -> {
                Map<String, Long> result = new HashMap<>();
                while (rs.next()) {
                    result.put(rs.getString("status"), rs.getLong("count_num"));
                }
                return result;
            });
        } catch (DataAccessException e) {
            log.error("Failed to execute JDBC status breakdown query: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Error fetching status breakdown via JDBC", e);
        }
    }

    /**
     * Inner RowMapper demonstrating explicit ResultSet column extraction.
     */
    private static class AnalyticsOverviewRowMapper implements RowMapper<AnalyticsOverviewResponse> {
        @Override
        public AnalyticsOverviewResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new AnalyticsOverviewResponse(
                    rs.getLong("total_users"),
                    rs.getLong("total_patients"),
                    rs.getLong("total_professionals"),
                    rs.getLong("active_users"),
                    rs.getLong("total_appointments"),
                    rs.getLong("completed_appointments"),
                    rs.getLong("cancelled_appointments"),
                    rs.getLong("pending_appointments"),
                    rs.getLong("today_appointments")
            );
        }
    }
}
