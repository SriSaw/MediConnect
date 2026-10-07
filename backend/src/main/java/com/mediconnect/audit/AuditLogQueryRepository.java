package com.mediconnect.audit;

import com.mediconnect.common.PageResponse;
import org.springframework.data.domain.Pageable;

/**
 * Interface abstraction for audit log queries and paginated historical reporting.
 * Follows OOP abstraction principles and enables decoupling between service logic
 * and low-level JDBC persistence operations.
 */
public interface AuditLogQueryRepository {

    /**
     * Retrieves paginated audit logs with optional filtering criteria using JDBC.
     *
     * @param action       optional action filter (e.g. USER_CREATED, APPOINTMENT_BOOKED)
     * @param resourceType optional resource type filter (e.g. USER, APPOINTMENT)
     * @param pageable     pagination information (page, size)
     * @return paginated response containing AuditLogResponse DTOs
     */
    PageResponse<AuditLogResponse> findAuditLogs(String action, String resourceType, Pageable pageable);
}
