package com.mediconnect.audit;

import com.mediconnect.common.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long actorUserId, String action, String resourceType, String resourceId, String ipAddress) {
        try {
            AuditLog auditLog = new AuditLog(actorUserId, action, resourceType, resourceId, ipAddress);
            auditLogRepository.save(auditLog);
            log.info("Audit logged: user={}, action={}, type={}, id={}",
                    actorUserId, action, resourceType, resourceId);
        } catch (Exception e) {
            log.error("Failed to persist audit log: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getAuditLogs(Pageable pageable) {
        return PageResponse.from(auditLogRepository.findAllByOrderByTimestampDesc(pageable)
                .map(AuditLogResponse::from));
    }
}
