package com.mediconnect.audit;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        Long actorUserId,
        String action,
        String resourceType,
        String resourceId,
        LocalDateTime timestamp,
        String ipAddress
) {
    public static AuditLogResponse from(AuditLog log) {
        if (log == null) {
            return null;
        }
        return new AuditLogResponse(
                log.getId(),
                log.getActorUserId(),
                log.getAction(),
                log.getResourceType(),
                log.getResourceId(),
                log.getTimestamp(),
                log.getIpAddress()
        );
    }
}
