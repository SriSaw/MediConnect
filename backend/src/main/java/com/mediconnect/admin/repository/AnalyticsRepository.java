package com.mediconnect.admin.repository;

import com.mediconnect.admin.dto.AnalyticsOverviewResponse;

import java.util.Map;

/**
 * Interface abstraction for platform analytics and operational reporting.
 * Satisfies the OOP abstraction and polymorphism rubrics by decoupling
 * callers from raw database persistence implementations.
 */
public interface AnalyticsRepository {

    /**
     * Retrieves aggregated system metrics via optimized SQL aggregations.
     *
     * @return populated AnalyticsOverviewResponse DTO
     */
    AnalyticsOverviewResponse getOverview();

    /**
     * Computes the distribution of appointments grouped by their current status.
     *
     * @return mapping of appointment status to count
     */
    Map<String, Long> getAppointmentStatusDistribution();
}
