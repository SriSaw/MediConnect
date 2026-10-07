package com.mediconnect.auth;

import com.mediconnect.audit.AuditLogService;
import com.mediconnect.auth.dto.AuthResponse;
import com.mediconnect.auth.dto.LoginRequest;
import com.mediconnect.auth.dto.RegisterRequest;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.exception.ConflictException;
import com.mediconnect.exception.UnauthorizedException;
import com.mediconnect.notification.NotificationService;
import com.mediconnect.patient.PatientProfileRepository;
import com.mediconnect.professional.ProfessionalProfileRepository;
import com.mediconnect.security.JwtTokenProvider;
import com.mediconnect.security.UserPrincipal;
import com.mediconnect.user.Role;
import com.mediconnect.user.User;
import com.mediconnect.user.UserRepository;
import com.mediconnect.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PatientProfileRepository patientProfileRepository;

    @Mock
    private ProfessionalProfileRepository professionalProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private NotificationService notificationService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                patientProfileRepository,
                professionalProfileRepository,
                passwordEncoder,
                authenticationManager,
                tokenProvider,
                auditLogService,
                notificationService,
                900000L
        );
    }

    @Test
    @DisplayName("Should successfully register a patient")
    void testRegisterPatient_Success() {
        RegisterRequest request = new RegisterRequest(
                "Jane Doe", "jane@test.local", "Password123!", "+1-555-0199", Role.PATIENT
        );

        when(userRepository.existsByEmail("jane@test.local")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashed-pass");

        User savedUser = new User("Jane Doe", "jane@test.local", "hashed-pass", "+1-555-0199", Role.PATIENT, UserStatus.ACTIVE);
        savedUser.setId(10L);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(tokenProvider.generateAccessToken(any(UserPrincipal.class))).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(any(UserPrincipal.class))).thenReturn("refresh-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals("Jane Doe", response.user().name());
        verify(patientProfileRepository).save(any());
        verify(auditLogService).log(eq(10L), eq("USER_CREATED"), eq("USER"), eq("10"), any());
    }

    @Test
    @DisplayName("Security Test #8: Duplicate email registration fails with ConflictException")
    void testRegisterDuplicateEmail_Fails() {
        RegisterRequest request = new RegisterRequest(
                "Jane Doe", "duplicate@test.local", "Password123!", null, Role.PATIENT
        );

        when(userRepository.existsByEmail("duplicate@test.local")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should forbid self-registration as ADMIN")
    void testRegisterAdmin_Forbidden() {
        RegisterRequest request = new RegisterRequest(
                "Attacker", "hacker@test.local", "Password123!", null, Role.ADMIN
        );

        when(userRepository.existsByEmail("hacker@test.local")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully authenticate valid user and issue JWT tokens")
    void testLogin_Success() {
        LoginRequest request = new LoginRequest("user@test.local", "Password123!");

        User user = new User("User", "user@test.local", "hashed-pass", null, Role.PATIENT, UserStatus.ACTIVE);
        user.setId(5L);

        when(userRepository.findByEmail("user@test.local")).thenReturn(Optional.of(user));

        UserPrincipal principal = UserPrincipal.create(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(tokenProvider.generateAccessToken(principal)).thenReturn("jwt-access");
        when(tokenProvider.generateRefreshToken(principal)).thenReturn("jwt-refresh");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-access", response.accessToken());
        assertEquals("jwt-refresh", response.refreshToken());
    }

    @Test
    @DisplayName("Security Test #10: Inactive or suspended user cannot authenticate")
    void testLoginInactiveUser_Fails() {
        LoginRequest request = new LoginRequest("suspended@test.local", "Password123!");

        User suspendedUser = new User("Suspended", "suspended@test.local", "hashed-pass", null, Role.PATIENT, UserStatus.SUSPENDED);
        suspendedUser.setId(8L);

        when(userRepository.findByEmail("suspended@test.local")).thenReturn(Optional.of(suspendedUser));

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
        verify(authenticationManager, never()).authenticate(any());
    }
}
