package com.mediconnect.audit;

import com.mediconnect.common.PageResponse;
import com.mediconnect.exception.DatabaseOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of AuditLogQueryRepository.
 * Demonstrates:
 * - Parameterized PreparedStatement queries via JdbcTemplate to prevent SQL injection
 * - Dynamic SQL condition composition based on filter parameters
 * - Explicit RowMapper extracting ResultSet columns to typed AuditLogResponse DTOs
 * - Paginated database access using SQL LIMIT and OFFSET
 * - DataAccessException translation to domain DatabaseOperationException
 */
@Repository
public class JdbcAuditLogRepository implements AuditLogQueryRepository {

    private static final Logger log = LoggerFactory.getLogger(JdbcAuditLogRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public JdbcAuditLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PageResponse<AuditLogResponse> findAuditLogs(String action, String resourceType, Pageable pageable) {
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");
        List<Object> countParams = new ArrayList<>();

        if (action != null && !action.isBlank()) {
            whereClause.append(" AND action = ? ");
            countParams.add(action.trim());
        }

        if (resourceType != null && !resourceType.isBlank()) {
            whereClause.append(" AND resource_type = ? ");
            countParams.add(resourceType.trim());
        }

        String countSql = "SELECT COUNT(*) FROM audit_logs " + whereClause;

        try {
            Long totalNullable = jdbcTemplate.queryForObject(countSql, Long.class, countParams.toArray());
            long total = totalNullable != null ? totalNullable : 0L;

            int limit = pageable.getPageSize();
            long offset = pageable.getOffset();

            String querySql = "SELECT id, actor_user_id, action, resource_type, resource_id, timestamp, ip_address " +
                              "FROM audit_logs " +
                              whereClause +
                              " ORDER BY timestamp DESC LIMIT ? OFFSET ?";

            List<Object> queryParams = new ArrayList<>(countParams);
            queryParams.add(limit);
            queryParams.add(offset);

            List<AuditLogResponse> content = jdbcTemplate.query(
                    querySql,
                    new AuditLogRowMapper(),
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
            log.error("Failed to execute JDBC audit log query: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Error querying audit logs via JDBC", e);
        }
    }

    /**
     * RowMapper mapping audit_logs table records to immutable AuditLogResponse records.
     */
    private static class AuditLogRowMapper implements RowMapper<AuditLogResponse> {
        @Override
        public AuditLogResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long actorUserId = rs.getLong("actor_user_id");
            if (rs.wasNull()) {
                actorUserId = null;
            }

            Timestamp ts = rs.getTimestamp("timestamp");
            LocalDateTime timestamp = ts != null ? ts.toLocalDateTime() : null;

            return new AuditLogResponse(
                    rs.getLong("id"),
                    actorUserId,
                    rs.getString("action"),
                    rs.getString("resource_type"),
                    rs.getString("resource_id"),
                    timestamp,
                    rs.getString("ip_address")
            );
        }
    }
}
