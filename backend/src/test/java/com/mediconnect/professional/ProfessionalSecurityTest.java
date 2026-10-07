package com.mediconnect.professional;

import com.mediconnect.audit.AuditLogService;
import com.mediconnect.exception.ForbiddenException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.professional.dto.AvailabilityRequest;
import com.mediconnect.security.UserPrincipal;
import com.mediconnect.user.Role;
import com.mediconnect.user.User;
import com.mediconnect.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfessionalSecurityTest {

    @Mock
    private ProfessionalProfileRepository professionalProfileRepository;

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private com.mediconnect.user.UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    private ProfessionalService professionalService;

    private User docUserA;
    private User docUserB;
    private ProfessionalProfile docProfileA;
    private ProfessionalProfile docProfileB;
    private Availability availabilityB;

    @BeforeEach
    void setUp() {
        professionalService = new ProfessionalService(
                professionalProfileRepository,
                availabilityRepository,
                userRepository,
                auditLogService
        );

        docUserA = new User("Dr. Alpha", "docA@test.local", "hash", null, Role.HEALTHCARE_PROFESSIONAL, UserStatus.ACTIVE);
        docUserA.setId(10L);
        docProfileA = new ProfessionalProfile(docUserA, "Cardiology", "LIC-A", 10, "Bio", BigDecimal.valueOf(100), true);
        docProfileA.setId(100L);

        docUserB = new User("Dr. Beta", "docB@test.local", "hash", null, Role.HEALTHCARE_PROFESSIONAL, UserStatus.ACTIVE);
        docUserB.setId(20L);
        docProfileB = new ProfessionalProfile(docUserB, "Dermatology", "LIC-B", 5, "Bio", BigDecimal.valueOf(80), true);
        docProfileB.setId(200L);

        availabilityB = new Availability(docProfileB, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0), true);
        availabilityB.setId(555L);
    }

    @Test
    @DisplayName("Security Test #2: Professional A cannot update Professional B's availability")
    void testProfessionalACannotUpdateProfessionalBsAvailability() {
        UserPrincipal docPrincipalA = UserPrincipal.create(docUserA);

        when(professionalProfileRepository.findByUserId(10L)).thenReturn(Optional.of(docProfileA));
        when(availabilityRepository.findById(555L)).thenReturn(Optional.of(availabilityB));

        AvailabilityRequest updateReq = new AvailabilityRequest(DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(14, 0), true);

        assertThrows(ForbiddenException.class, () ->
                professionalService.updateAvailability(docPrincipalA, 555L, updateReq));
        verify(availabilityRepository, never()).save(any());
    }

    @Test
    @DisplayName("Security Test #2: Professional A cannot delete Professional B's availability")
    void testProfessionalACannotDeleteProfessionalBsAvailability() {
        UserPrincipal docPrincipalA = UserPrincipal.create(docUserA);

        when(professionalProfileRepository.findByUserId(10L)).thenReturn(Optional.of(docProfileA));
        when(availabilityRepository.findById(555L)).thenReturn(Optional.of(availabilityB));

        assertThrows(ForbiddenException.class, () ->
                professionalService.deleteAvailability(docPrincipalA, 555L));
        verify(availabilityRepository, never()).delete(any());
    }
}
