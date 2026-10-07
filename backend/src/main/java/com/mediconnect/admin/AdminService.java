package com.mediconnect.admin;

import com.mediconnect.admin.dto.*;
import com.mediconnect.appointment.AppointmentRepository;
import com.mediconnect.appointment.AppointmentStatus;
import com.mediconnect.audit.AuditLogService;
import com.mediconnect.common.PageResponse;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.exception.ConflictException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.patient.PatientProfile;
import com.mediconnect.patient.PatientProfileRepository;
import com.mediconnect.professional.ProfessionalProfile;
import com.mediconnect.professional.ProfessionalProfileRepository;
import com.mediconnect.user.Role;
import com.mediconnect.user.User;
import com.mediconnect.user.UserRepository;
import com.mediconnect.user.UserStatus;
import com.mediconnect.user.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.mediconnect.admin.repository.AnalyticsRepository;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final AnalyticsRepository analyticsRepository;

    public AdminService(
            UserRepository userRepository,
            PatientProfileRepository patientProfileRepository,
            ProfessionalProfileRepository professionalProfileRepository,
            AppointmentRepository appointmentRepository,
            PasswordEncoder passwordEncoder,
            AuditLogService auditLogService,
            AnalyticsRepository analyticsRepository
    ) {
        this.userRepository = userRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.professionalProfileRepository = professionalProfileRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
        this.analyticsRepository = analyticsRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsers(Role role, UserStatus status, String query, Pageable pageable) {
        String searchQuery = (query != null && !query.isBlank()) ? query.trim() : null;
        Page<User> page = userRepository.searchUsers(role, status, searchQuery, pageable);
        return PageResponse.from(page.map(UserResponse::from));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserAdminRequest request, Long adminId) {
        String normalizedEmail = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Email is already registered: " + normalizedEmail);
        }

        User user = new User(
                request.name().trim(),
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                request.phone() != null ? request.phone().trim() : null,
                request.role(),
                request.status() != null ? request.status() : UserStatus.ACTIVE
        );

        User savedUser = userRepository.save(user);

        if (request.role() == Role.PATIENT) {
            patientProfileRepository.save(new PatientProfile(savedUser));
        } else if (request.role() == Role.HEALTHCARE_PROFESSIONAL) {
            professionalProfileRepository.save(new ProfessionalProfile(
                    savedUser,
                    "General Practice",
                    "LIC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                    0,
                    "Professional profile created by administrator.",
                    BigDecimal.valueOf(50.00),
                    true
            ));
        }

        auditLogService.log(adminId, "USER_CREATED", "USER", savedUser.getId().toString(), null);
        return UserResponse.from(savedUser);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserAdminRequest request, Long adminId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (request.email() != null && !request.email().isBlank()) {
            String newEmail = request.email().trim().toLowerCase();
            if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new ConflictException("Email already in use: " + newEmail);
            }
            user.setEmail(newEmail);
        }

        user.setName(request.name().trim());
        if (request.phone() != null) {
            user.setPhone(request.phone().trim());
        }

        User updated = userRepository.save(user);
        auditLogService.log(adminId, "USER_UPDATED", "USER", id.toString(), null);
        return UserResponse.from(updated);
    }

    @Transactional
    public UserResponse updateUserStatus(Long id, UpdateUserStatusRequest request, Long adminId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Safeguard: Check if deactivating final active admin
        if (user.getRole() == Role.ADMIN && request.status() != UserStatus.ACTIVE) {
            long activeAdminCount = userRepository.countActiveByRole(Role.ADMIN, UserStatus.ACTIVE);
            if (activeAdminCount <= 1) {
                throw new BadRequestException("Action denied: Cannot deactivate or suspend the final active administrator");
            }
        }

        user.setStatus(request.status());
        User updated = userRepository.save(user);
        auditLogService.log(adminId, "USER_STATUS_CHANGED", "USER", id.toString(), null);
        return UserResponse.from(updated);
    }

    @Transactional
    public UserResponse updateUserRole(Long id, UpdateUserRoleRequest request, Long adminId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Safeguard: Check if demoting final active admin
        if (user.getRole() == Role.ADMIN && request.role() != Role.ADMIN) {
            long activeAdminCount = userRepository.countActiveByRole(Role.ADMIN, UserStatus.ACTIVE);
            if (activeAdminCount <= 1) {
                throw new BadRequestException("Action denied: Cannot change the role of the final active administrator");
            }
        }

        Role oldRole = user.getRole();
        user.setRole(request.role());

        // Create profile if newly assigned role requires one
        if (request.role() == Role.PATIENT && !patientProfileRepository.existsByUserId(id)) {
            patientProfileRepository.save(new PatientProfile(user));
        } else if (request.role() == Role.HEALTHCARE_PROFESSIONAL && !professionalProfileRepository.existsByUserId(id)) {
            professionalProfileRepository.save(new ProfessionalProfile(
                    user,
                    "General Practice",
                    "LIC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                    0,
                    "Professional profile created on role assignment.",
                    BigDecimal.valueOf(50.00),
                    false
            ));
        }

        User updated = userRepository.save(user);
        auditLogService.log(adminId, "ROLE_CHANGED", "USER", id.toString(), null);
        return UserResponse.from(updated);
    }

    @Transactional
    public void verifyProfessional(Long id, boolean verified, Long adminId) {
        ProfessionalProfile profile = professionalProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalProfile", "id", id));

        profile.setVerified(verified);
        professionalProfileRepository.save(profile);
        auditLogService.log(adminId, "ROLE_CHANGED", "PROFESSIONAL", id.toString(), null);
    }

    @Transactional(readOnly = true)
    public AnalyticsOverviewResponse getAnalyticsOverview() {
        // Leverages JDBC-backed aggregated SQL implementation via AnalyticsRepository abstraction
        return analyticsRepository.getOverview();
    }
}
