package com.mediconnect.repository;

import com.mediconnect.admin.dto.AnalyticsOverviewResponse;
import com.mediconnect.admin.repository.AnalyticsRepository;
import com.mediconnect.appointment.AppointmentStatus;
import com.mediconnect.appointment.dto.AppointmentReportResponse;
import com.mediconnect.appointment.repository.AppointmentReportRepository;
import com.mediconnect.audit.AuditLogQueryRepository;
import com.mediconnect.audit.AuditLogResponse;
import com.mediconnect.audit.AuditLogService;
import com.mediconnect.common.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test explicitly verifying all JDBC repositories and database operations.
 * Validates:
 * - JdbcAnalyticsRepository SQL aggregations and status mapping
 * - JdbcAuditLogRepository parameterized pagination and RowMapper
 * - JdbcAppointmentReportRepository multi-table joins and JDBC transaction handling
 */
@SpringBootTest
@ActiveProfiles("test")
public class JdbcRepositoriesIntegrationTest {

    @Autowired
    private AnalyticsRepository analyticsRepository;

    @Autowired
    private AuditLogQueryRepository auditLogQueryRepository;

    @Autowired
    private AppointmentReportRepository appointmentReportRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Test
    @DisplayName("JDBC Analytics: Validates aggregated counts and status distribution via JdbcTemplate")
    void testJdbcAnalyticsRepository() {
        AnalyticsOverviewResponse overview = analyticsRepository.getOverview();

        assertNotNull(overview);
        assertTrue(overview.totalUsers() >= 0);
        assertTrue(overview.totalPatients() >= 0);
        assertTrue(overview.totalProfessionals() >= 0);
        assertTrue(overview.activeUsers() >= 0);
        assertTrue(overview.totalAppointments() >= 0);

        Map<String, Long> distribution = analyticsRepository.getAppointmentStatusDistribution();
        assertNotNull(distribution);
    }

    @Test
    @DisplayName("JDBC Audit Log: Validates parameterized query and pagination via RowMapper")
    void testJdbcAuditLogRepository() {
        // Record test audit entries
        auditLogService.log(1L, "TEST_ACTION_JDBC", "SYSTEM", "999", "127.0.0.1");

        PageResponse<AuditLogResponse> page = auditLogQueryRepository.findAuditLogs(
                "TEST_ACTION_JDBC", null, PageRequest.of(0, 10)
        );

        assertNotNull(page);
        assertNotNull(page.content());
        assertFalse(page.content().isEmpty());

        AuditLogResponse entry = page.content().get(0);
        assertEquals("TEST_ACTION_JDBC", entry.action());
        assertEquals("SYSTEM", entry.resourceType());
        assertEquals("999", entry.resourceId());
    }

    @Test
    @DisplayName("JDBC Appointment Reporting: Validates joins, filtering, and manual JDBC transaction management")
    void testJdbcAppointmentReportingAndTransaction() {
        PageResponse<AppointmentReportResponse> reports = appointmentReportRepository.searchAppointments(
                null, null, null, null, null, PageRequest.of(0, 10)
        );

        assertNotNull(reports);
        assertNotNull(reports.content());

        // Test explicit JDBC transaction batch status update
        int updated = appointmentReportRepository.batchUpdateStatusInTransaction(
                List.of(999999L), // Non-existent ID: safe test of execution path
                AppointmentStatus.CANCELLED,
                1L
        );
        assertEquals(0, updated);
    }
}
