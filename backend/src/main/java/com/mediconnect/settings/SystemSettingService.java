package com.mediconnect.settings;

import com.mediconnect.audit.AuditLogService;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.settings.dto.SystemSettingRequest;
import com.mediconnect.settings.dto.SystemSettingResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class SystemSettingService {

    private static final Set<String> ALLOWED_KEYS = Set.of(
            "appointment_duration_minutes",
            "platform_name",
            "max_booking_advance_days",
            "support_email",
            "allow_new_registrations"
    );

    private final SystemSettingRepository systemSettingRepository;
    private final AuditLogService auditLogService;

    public SystemSettingService(SystemSettingRepository systemSettingRepository, AuditLogService auditLogService) {
        this.systemSettingRepository = systemSettingRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<SystemSettingResponse> getAllSettings() {
        return systemSettingRepository.findAll()
                .stream()
                .map(SystemSettingResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SystemSettingResponse getSettingByKey(String key) {
        SystemSetting setting = systemSettingRepository.findByKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("SystemSetting", "key", key));
        return SystemSettingResponse.from(setting);
    }

    @Transactional
    public SystemSettingResponse updateSetting(Long actorUserId, String key, SystemSettingRequest request) {
        if (!ALLOWED_KEYS.contains(key)) {
            throw new BadRequestException("Unrecognized or restricted system setting key: " + key);
        }

        SystemSetting setting = systemSettingRepository.findByKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("SystemSetting", "key", key));

        setting.setValue(request.value().trim());
        if (request.description() != null && !request.description().isBlank()) {
            setting.setDescription(request.description().trim());
        }

        SystemSetting updated = systemSettingRepository.save(setting);
        auditLogService.log(actorUserId, "SYSTEM_SETTING_UPDATED", "SYSTEM_SETTING", key, null);

        return SystemSettingResponse.from(updated);
    }
}
