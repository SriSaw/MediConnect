package com.mediconnect.admin;

import com.mediconnect.admin.dto.*;
import com.mediconnect.audit.AuditLogResponse;
import com.mediconnect.audit.AuditLogService;
import com.mediconnect.common.PageResponse;
import com.mediconnect.security.CurrentUser;
import com.mediconnect.security.UserPrincipal;
import com.mediconnect.settings.SystemSettingService;
import com.mediconnect.settings.dto.SystemSettingRequest;
import com.mediconnect.settings.dto.SystemSettingResponse;
import com.mediconnect.user.Role;
import com.mediconnect.user.UserStatus;
import com.mediconnect.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Administration, analytics, audit logs, and settings endpoints")
public class AdminController {

    private final AdminService adminService;
    private final SystemSettingService systemSettingService;
    private final AuditLogService auditLogService;

    public AdminController(AdminService adminService,
                           SystemSettingService systemSettingService,
                           AuditLogService auditLogService) {
        this.adminService = adminService;
        this.systemSettingService = systemSettingService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/users")
    @Operation(summary = "List and search all users with pagination")
    public ResponseEntity<PageResponse<UserResponse>> getUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String query,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(adminService.getUsers(role, status, query, pageable));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user details by ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @PostMapping("/users")
    @Operation(summary = "Create a new user as administrator")
    public ResponseEntity<UserResponse> createUser(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateUserAdminRequest request
    ) {
        UserResponse response = adminService.createUser(request, currentUser.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "Update user basic information")
    public ResponseEntity<UserResponse> updateUser(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserAdminRequest request
    ) {
        return ResponseEntity.ok(adminService.updateUser(id, request, currentUser.getId()));
    }

    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Change user status (active/inactive/suspended)")
    public ResponseEntity<UserResponse> updateUserStatus(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        return ResponseEntity.ok(adminService.updateUserStatus(id, request, currentUser.getId()));
    }

    @PatchMapping("/users/{id}/role")
    @Operation(summary = "Change user role")
    public ResponseEntity<UserResponse> updateUserRole(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        return ResponseEntity.ok(adminService.updateUserRole(id, request, currentUser.getId()));
    }

    @PatchMapping("/professionals/{id}/verify")
    @Operation(summary = "Verify or unverify healthcare professional profile")
    public ResponseEntity<Void> verifyProfessional(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody VerifyProfessionalRequest request
    ) {
        adminService.verifyProfessional(id, request.verified(), currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/analytics/overview")
    @Operation(summary = "Get dashboard analytics overview")
    public ResponseEntity<AnalyticsOverviewResponse> getAnalyticsOverview() {
        return ResponseEntity.ok(adminService.getAnalyticsOverview());
    }

    @GetMapping("/settings")
    @Operation(summary = "Get all system settings")
    public ResponseEntity<List<SystemSettingResponse>> getSettings() {
        return ResponseEntity.ok(systemSettingService.getAllSettings());
    }

    @PutMapping("/settings/{key}")
    @Operation(summary = "Update a specific system setting")
    public ResponseEntity<SystemSettingResponse> updateSetting(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable String key,
            @Valid @RequestBody SystemSettingRequest request
    ) {
        return ResponseEntity.ok(systemSettingService.updateSetting(currentUser.getId(), key, request));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "List system audit logs with pagination")
    public ResponseEntity<PageResponse<AuditLogResponse>> getAuditLogs(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(auditLogService.getAuditLogs(pageable));
    }
}
