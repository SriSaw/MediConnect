package com.mediconnect.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for SystemDiagnosticsServlet.
 * Validates HttpServlet lifecycle, request/response headers, and low-level PrintWriter execution.
 */
public class SystemDiagnosticsServletTest {

    @Test
    @DisplayName("Servlet: Validates HttpServlet doGet lifecycle, headers, and JSON output generation")
    void testServletExecution() throws Exception {
        DataSource mockDataSource = mock(DataSource.class);
        Connection mockConn = mock(Connection.class);
        DatabaseMetaData mockMeta = mock(DatabaseMetaData.class);

        when(mockDataSource.getConnection()).thenReturn(mockConn);
        when(mockConn.getMetaData()).thenReturn(mockMeta);
        when(mockMeta.getDatabaseProductName()).thenReturn("PostgreSQL");
        when(mockMeta.getDatabaseProductVersion()).thenReturn("16.0");
        when(mockConn.isClosed()).thenReturn(false);

        SystemDiagnosticsServlet servlet = new SystemDiagnosticsServlet(mockDataSource);
        servlet.init(new MockServletConfig("diagnosticsServlet"));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/system/servlet-diagnostics");
        request.addHeader("User-Agent", "JUnit-TestEngine");
        request.setParameter("action", "test");

        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.doGet(request, response);

        assertEquals(200, response.getStatus());
        assertEquals("application/json;charset=UTF-8", response.getContentType());
        assertTrue(response.getHeaderNames().contains("X-Servlet-Engine"));
        assertTrue(response.getContentAsString().contains("diagnosticsServlet"));
        assertTrue(response.getContentAsString().contains("PostgreSQL"));

        servlet.destroy();
    }
}
