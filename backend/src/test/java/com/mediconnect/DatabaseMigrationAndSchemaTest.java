package com.mediconnect;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseMigrationAndSchemaTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("Verify Flyway applied all 11 schema migrations successfully")
    void testAllFlywayTablesCreated() throws Exception {
        Set<String> tableNames = new HashSet<>();
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tableNames.add(rs.getString("TABLE_NAME").toLowerCase());
                }
            }
        }

        assertTrue(tableNames.contains("users"), "users table should exist");
        assertTrue(tableNames.contains("patient_profiles"), "patient_profiles table should exist");
        assertTrue(tableNames.contains("professional_profiles"), "professional_profiles table should exist");
        assertTrue(tableNames.contains("availabilities"), "availabilities table should exist");
        assertTrue(tableNames.contains("appointments"), "appointments table should exist");
        assertTrue(tableNames.contains("consultations"), "consultations table should exist");
        assertTrue(tableNames.contains("medical_records"), "medical_records table should exist");
        assertTrue(tableNames.contains("notifications"), "notifications table should exist");
        assertTrue(tableNames.contains("messages"), "messages table should exist");
        assertTrue(tableNames.contains("system_settings"), "system_settings table should exist");
        assertTrue(tableNames.contains("audit_logs"), "audit_logs table should exist");
    }
}
