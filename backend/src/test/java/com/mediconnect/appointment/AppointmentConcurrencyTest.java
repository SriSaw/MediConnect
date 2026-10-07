package com.mediconnect.appointment;

import com.mediconnect.appointment.dto.AppointmentResponse;
import com.mediconnect.appointment.dto.CreateAppointmentRequest;
import com.mediconnect.exception.ConflictException;
import com.mediconnect.patient.PatientProfile;
import com.mediconnect.patient.PatientProfileRepository;
import com.mediconnect.professional.Availability;
import com.mediconnect.professional.AvailabilityRepository;
import com.mediconnect.professional.ProfessionalProfile;
import com.mediconnect.professional.ProfessionalProfileRepository;
import com.mediconnect.security.UserPrincipal;
import com.mediconnect.user.Role;
import com.mediconnect.user.User;
import com.mediconnect.user.UserRepository;
import com.mediconnect.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Concurrency and Synchronization Verification Test.
 * Satisfies the Multithreading & Synchronization College Rubric (4 marks).
 *
 * Simulates concurrent booking requests from multiple patient threads
 * competing for the identical doctor and time slot.
 * Verifies that the pessimistic locking and transaction isolation prevent race conditions
 * and double booking. Exactly 1 request must succeed, and all others must fail.
 */
@SpringBootTest
@ActiveProfiles("test")
public class AppointmentConcurrencyTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientProfileRepository patientProfileRepository;

    @Autowired
    private ProfessionalProfileRepository professionalProfileRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private ProfessionalProfile doctorProfile;
    private final List<PatientProfile> testPatients = new ArrayList<>();
    private LocalDate targetDate;
    private final LocalTime startTime = LocalTime.of(14, 0);
    private final LocalTime endTime = LocalTime.of(14, 30);

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();
        availabilityRepository.deleteAll();

        // Target a weekday at least 7 days in the future
        targetDate = LocalDate.now().plusDays(7);
        while (targetDate.getDayOfWeek() == DayOfWeek.SATURDAY || targetDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            targetDate = targetDate.plusDays(1);
        }

        // Create or retrieve doctor
        String doctorEmail = "concurrent.doctor@mediconnect.local";
        User doctorUser = userRepository.findByEmail(doctorEmail).orElseGet(() ->
                userRepository.save(new User("Concurrent Doctor", doctorEmail, "passwordHash", "123", Role.HEALTHCARE_PROFESSIONAL, UserStatus.ACTIVE))
        );

        doctorProfile = professionalProfileRepository.findByUserId(doctorUser.getId()).orElseGet(() ->
                professionalProfileRepository.save(new ProfessionalProfile(
                        doctorUser, "General Medicine", "LIC-CONC-01", 10, "Bio", BigDecimal.valueOf(75.0), true
                ))
        );

        // Ensure covering availability exists for doctor on target day
        availabilityRepository.save(new Availability(
                doctorProfile, targetDate.getDayOfWeek(), LocalTime.of(8, 0), LocalTime.of(18, 0), true
        ));

        // Create 5 distinct patients for concurrency contention
        testPatients.clear();
        for (int i = 1; i <= 5; i++) {
            final int index = i;
            String patientEmail = "concurrent.patient" + index + "@mediconnect.local";
            User patientUser = userRepository.findByEmail(patientEmail).orElseGet(() ->
                    userRepository.save(new User("Patient " + index, patientEmail, "passwordHash", "123", Role.PATIENT, UserStatus.ACTIVE))
            );

            PatientProfile profile = patientProfileRepository.findByUserId(patientUser.getId()).orElseGet(() ->
                    patientProfileRepository.save(new PatientProfile(patientUser))
            );
            testPatients.add(profile);
        }
    }

    @Test
    @DisplayName("Multithreading: Concurrent appointment booking prevents double booking under high contention")
    void testConcurrentBookingForSameSlot() throws InterruptedException {
        int threadCount = testPatients.size();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        AtomicInteger otherErrors = new AtomicInteger(0);
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            PatientProfile patient = testPatients.get(i);
            UserPrincipal principal = UserPrincipal.create(patient.getUser());
            CreateAppointmentRequest request = new CreateAppointmentRequest(
                    doctorProfile.getId(),
                    targetDate,
                    startTime,
                    endTime,
                    "Contention booking test from thread " + i
            );

            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    // Block until all threads are ready, then release simultaneously
                    startLatch.await();
                    AppointmentResponse resp = appointmentService.bookAppointment(principal, request);
                    if (resp != null) {
                        successCount.incrementAndGet();
                    }
                } catch (ConflictException ce) {
                    conflictCount.incrementAndGet();
                } catch (Exception e) {
                    otherErrors.incrementAndGet();
                    exceptions.add(e);
                }
            });
        }

        // Wait for all worker threads to reach ready barrier
        assertTrue(readyLatch.await(5, TimeUnit.SECONDS), "Threads failed to prepare in time");

        // Release the barrier to fire all requests simultaneously
        startLatch.countDown();

        executor.shutdown();
        assertTrue(executor.awaitTermination(15, TimeUnit.SECONDS), "Concurrent test execution timed out");

        // Assert exactly 1 booking succeeded and all others failed with ConflictException
        assertEquals(1, successCount.get(), "Expected exactly 1 booking to succeed");
        assertEquals(threadCount - 1, conflictCount.get(), "Expected remaining threads to fail with ConflictException");
        assertEquals(0, otherErrors.get(), "Unexpected errors encountered: " + exceptions);

        // Verify database state contains exactly 1 appointment for this slot
        long persistedCount = appointmentRepository.count();
        assertEquals(1, persistedCount, "Database must persist exactly 1 appointment record");
    }
}
