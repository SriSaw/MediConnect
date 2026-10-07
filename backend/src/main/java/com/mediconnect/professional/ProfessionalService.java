package com.mediconnect.professional;

import com.mediconnect.audit.AuditLogService;
import com.mediconnect.common.PageResponse;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.exception.ConflictException;
import com.mediconnect.exception.ForbiddenException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.professional.dto.AvailabilityRequest;
import com.mediconnect.professional.dto.AvailabilityResponse;
import com.mediconnect.professional.dto.ProfessionalProfileRequest;
import com.mediconnect.professional.dto.ProfessionalProfileResponse;
import com.mediconnect.security.UserPrincipal;
import com.mediconnect.user.User;
import com.mediconnect.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProfessionalService {

    private final ProfessionalProfileRepository professionalProfileRepository;
    private final AvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public ProfessionalService(
            ProfessionalProfileRepository professionalProfileRepository,
            AvailabilityRepository availabilityRepository,
            UserRepository userRepository,
            AuditLogService auditLogService
    ) {
        this.professionalProfileRepository = professionalProfileRepository;
        this.availabilityRepository = availabilityRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProfessionalProfileResponse> getProfessionals(
            String specialization, String search, Boolean verifiedOnly, Pageable pageable) {
        Page<ProfessionalProfile> page = professionalProfileRepository.searchProfessionals(
                specialization != null && !specialization.isBlank() ? specialization.trim() : null,
                search != null && !search.isBlank() ? search.trim() : null,
                verifiedOnly,
                pageable
        );
        return PageResponse.from(page.map(ProfessionalProfileResponse::from));
    }

    @Transactional(readOnly = true)
    public ProfessionalProfileResponse getProfessionalById(Long id) {
        ProfessionalProfile profile = professionalProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalProfile", "id", id));
        return ProfessionalProfileResponse.from(profile);
    }

    @Transactional(readOnly = true)
    public ProfessionalProfileResponse getMyProfile(UserPrincipal currentUser) {
        ProfessionalProfile profile = professionalProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalProfile", "userId", currentUser.getId()));
        return ProfessionalProfileResponse.from(profile);
    }

    @Transactional
    public ProfessionalProfileResponse updateMyProfile(UserPrincipal currentUser, ProfessionalProfileRequest request) {
        ProfessionalProfile profile = professionalProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalProfile", "userId", currentUser.getId()));

        if (request.specialization() != null && !request.specialization().isBlank()) {
            profile.setSpecialization(request.specialization().trim());
        }
        if (request.licenseNumber() != null && !request.licenseNumber().isBlank()) {
            String newLicense = request.licenseNumber().trim();
            if (!newLicense.equals(profile.getLicenseNumber()) && professionalProfileRepository.existsByLicenseNumber(newLicense)) {
                throw new ConflictException("License number is already in use by another professional");
            }
            profile.setLicenseNumber(newLicense);
        }
        if (request.experienceYears() != null) {
            profile.setExperienceYears(request.experienceYears());
        }
        if (request.bio() != null) {
            profile.setBio(request.bio().trim());
        }
        if (request.consultationFee() != null) {
            profile.setConsultationFee(request.consultationFee());
        }

        User user = profile.getUser();
        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name().trim());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone().trim());
        }
        userRepository.save(user);

        ProfessionalProfile updated = professionalProfileRepository.save(profile);
        auditLogService.log(currentUser.getId(), "USER_UPDATED", "PROFESSIONAL", profile.getId().toString(), null);
        return ProfessionalProfileResponse.from(updated);
    }

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getMyAvailability(UserPrincipal currentUser) {
        ProfessionalProfile profile = professionalProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalProfile", "userId", currentUser.getId()));
        return availabilityRepository.findByProfessionalIdOrderByDayOfWeekAscStartTimeAsc(profile.getId())
                .stream()
                .map(AvailabilityResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getProfessionalAvailability(Long professionalId) {
        if (!professionalProfileRepository.existsById(professionalId)) {
            throw new ResourceNotFoundException("ProfessionalProfile", "id", professionalId);
        }
        return availabilityRepository.findByProfessionalIdOrderByDayOfWeekAscStartTimeAsc(professionalId)
                .stream()
                .map(AvailabilityResponse::from)
                .toList();
    }

    @Transactional
    public AvailabilityResponse addAvailability(UserPrincipal currentUser, AvailabilityRequest request) {
        ProfessionalProfile profile = professionalProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalProfile", "userId", currentUser.getId()));

        validateAvailabilityTime(request);

        List<Availability> overlapping = availabilityRepository.findOverlappingAvailability(
                profile.getId(), request.dayOfWeek(), request.startTime(), request.endTime(), null);
        if (!overlapping.isEmpty()) {
            throw new ConflictException("Availability range overlaps with existing availability on " + request.dayOfWeek());
        }

        Availability availability = new Availability(
                profile,
                request.dayOfWeek(),
                request.startTime(),
                request.endTime(),
                request.available() == null || request.available()
        );

        Availability saved = availabilityRepository.save(availability);
        return AvailabilityResponse.from(saved);
    }

    @Transactional
    public AvailabilityResponse updateAvailability(UserPrincipal currentUser, Long id, AvailabilityRequest request) {
        Availability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Availability", "id", id));

        ProfessionalProfile profile = professionalProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalProfile", "userId", currentUser.getId()));

        if (!availability.getProfessional().getId().equals(profile.getId())) {
            throw new ForbiddenException("You are not authorized to modify another professional's availability");
        }

        validateAvailabilityTime(request);

        List<Availability> overlapping = availabilityRepository.findOverlappingAvailability(
                profile.getId(), request.dayOfWeek(), request.startTime(), request.endTime(), id);
        if (!overlapping.isEmpty()) {
            throw new ConflictException("Availability range overlaps with existing availability on " + request.dayOfWeek());
        }

        availability.setDayOfWeek(request.dayOfWeek());
        availability.setStartTime(request.startTime());
        availability.setEndTime(request.endTime());
        if (request.available() != null) {
            availability.setAvailable(request.available());
        }

        Availability updated = availabilityRepository.save(availability);
        return AvailabilityResponse.from(updated);
    }

    @Transactional
    public void deleteAvailability(UserPrincipal currentUser, Long id) {
        Availability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Availability", "id", id));

        ProfessionalProfile profile = professionalProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalProfile", "userId", currentUser.getId()));

        if (!availability.getProfessional().getId().equals(profile.getId())) {
            throw new ForbiddenException("You are not authorized to delete another professional's availability");
        }

        availabilityRepository.delete(availability);
    }

    @Transactional
    public ProfessionalProfileResponse verifyProfessional(Long id, boolean verified) {
        ProfessionalProfile profile = professionalProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalProfile", "id", id));
        profile.setVerified(verified);
        ProfessionalProfile updated = professionalProfileRepository.save(profile);
        return ProfessionalProfileResponse.from(updated);
    }

    private void validateAvailabilityTime(AvailabilityRequest request) {
        if (!request.startTime().isBefore(request.endTime())) {
            throw new BadRequestException("Start time (" + request.startTime() + ") must be before end time (" + request.endTime() + ")");
        }
    }
}
