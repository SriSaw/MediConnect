package com.mediconnect.security;

import com.mediconnect.user.Role;
import com.mediconnect.user.User;
import com.mediconnect.user.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Security Test #7: Unauthenticated request to protected endpoints is rejected with 401")
    void testUnauthenticatedAccessRejected() throws Exception {
        mockMvc.perform(get("/api/appointments/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/medical-records/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "patient@test.local", roles = {"PATIENT"})
    @DisplayName("Security Test #5: Patient cannot access admin APIs (403 Forbidden)")
    void testPatientCannotAccessAdminApis() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/analytics/overview"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/settings"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "doctor@test.local", roles = {"HEALTHCARE_PROFESSIONAL"})
    @DisplayName("Security Test #6: Professional cannot access admin APIs (403 Forbidden)")
    void testProfessionalCannotAccessAdminApis() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/analytics/overview"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/audit-logs"))
                .andExpect(status().isForbidden());
    }
}
