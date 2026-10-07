package com.mediconnect.admin.dto;

public record AnalyticsOverviewResponse(
        long totalUsers,
        long totalPatients,
        long totalProfessionals,
        long activeUsers,
        long totalAppointments,
        long completedAppointments,
        long cancelledAppointments,
        long pendingAppointments,
        long todayAppointments
) {}
