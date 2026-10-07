package com.mediconnect.auth;

import com.mediconnect.audit.AuditLogService;
import com.mediconnect.auth.dto.AuthResponse;
import com.mediconnect.auth.dto.LoginRequest;
import com.mediconnect.auth.dto.RefreshTokenRequest;
import com.mediconnect.auth.dto.RegisterRequest;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.exception.ConflictException;
import com.mediconnect.exception.UnauthorizedException;
import com.mediconnect.notification.NotificationService;
import com.mediconnect.notification.NotificationType;
import com.mediconnect.patient.PatientProfile;
import com.mediconnect.patient.PatientProfileRepository;
import com.mediconnect.professional.ProfessionalProfile;
import com.mediconnect.professional.ProfessionalProfileRepository;
import com.mediconnect.security.JwtTokenProvider;
import com.mediconnect.security.UserPrincipal;
import com.mediconnect.user.Role;
import com.mediconnect.user.User;
import com.mediconnect.user.UserRepository;
import com.mediconnect.user.UserStatus;
import com.mediconnect.user.dto.UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final long accessTokenExpirationMs;

    public AuthService(
            UserRepository userRepository,
            PatientProfileRepository patientProfileRepository,
            ProfessionalProfileRepository professionalProfileRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider tokenProvider,
            AuditLogService auditLogService,
            NotificationService notificationService,
            @Value("${app.jwt.access-token-expiration-ms:900000}") long accessTokenExpirationMs
    ) {
        this.userRepository = userRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.professionalProfileRepository = professionalProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Email is already registered: " + normalizedEmail);
        }

        if (request.role() == Role.ADMIN) {
            throw new BadRequestException("Self-registration as ADMIN is not permitted");
        }

        User user = new User(
                request.name().trim(),
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                request.phone() != null ? request.phone().trim() : null,
                request.role(),
                UserStatus.ACTIVE
        );
        User savedUser = userRepository.save(user);

        if (request.role() == Role.PATIENT) {
            PatientProfile patientProfile = new PatientProfile(savedUser);
            patientProfileRepository.save(patientProfile);
        } else if (request.role() == Role.HEALTHCARE_PROFESSIONAL) {
            ProfessionalProfile professionalProfile = new ProfessionalProfile(
                    savedUser,
                    "General Practice",
                    "LIC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                    0,
                    "Healthcare professional profile pending completion.",
                    BigDecimal.valueOf(50.00),
                    false
            );
            professionalProfileRepository.save(professionalProfile);
        }

        auditLogService.log(savedUser.getId(), "USER_CREATED", "USER", savedUser.getId().toString(), null);
        notificationService.createNotification(
                savedUser,
                "Welcome to MediConnect",
                "Your account has been successfully created. Welcome to our healthcare consultation platform.",
                NotificationType.GENERAL
        );

        UserPrincipal principal = UserPrincipal.create(savedUser);
        String accessToken = tokenProvider.generateAccessToken(principal);
        String refreshToken = tokenProvider.generateRefreshToken(principal);

        return AuthResponse.of(accessToken, refreshToken, accessTokenExpirationMs, UserResponse.from(savedUser));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account is inactive or suspended. Please contact administrator.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.password())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String accessToken = tokenProvider.generateAccessToken(principal);
        String refreshToken = tokenProvider.generateRefreshToken(principal);

        return AuthResponse.of(accessToken, refreshToken, accessTokenExpirationMs, UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        if (!tokenProvider.validateToken(request.refreshToken())) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String email = tokenProvider.getEmailFromToken(request.refreshToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found for refresh token"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("User account is no longer active");
        }

        UserPrincipal principal = UserPrincipal.create(user);
        String newAccessToken = tokenProvider.generateAccessToken(principal);

        return AuthResponse.of(newAccessToken, request.refreshToken(), accessTokenExpirationMs, UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));
        return UserResponse.from(user);
    }
}
