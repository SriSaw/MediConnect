package com.mediconnect.config;

import com.mediconnect.common.SystemDiagnosticsServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Registers custom low-level Jakarta Servlets into Spring Boot's embedded Tomcat container.
 * Demonstrates how traditional Servlets coexist harmoniously with Spring's DispatcherServlet.
 */
@Configuration
public class ServletConfig {

    @Bean
    public ServletRegistrationBean<SystemDiagnosticsServlet> systemDiagnosticsServletRegistration(DataSource dataSource) {
        SystemDiagnosticsServlet servlet = new SystemDiagnosticsServlet(dataSource);
        ServletRegistrationBean<SystemDiagnosticsServlet> registration = new ServletRegistrationBean<>(
                servlet,
                "/api/system/servlet-diagnostics"
        );
        registration.setName("systemDiagnosticsServlet");
        registration.setLoadOnStartup(1);
        return registration;
    }
}
