package com.company.orders.shared.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("WebConfig Tests")
class WebConfigTest {

    @Test
    @DisplayName("Should configure CORS mappings correctly")
    void shouldConfigureCorsCorrectly() {
        WebConfig webConfig = new WebConfig();
        CorsRegistry registry = mock(CorsRegistry.class);
        CorsRegistration registration = mock(CorsRegistration.class);

        when(registry.addMapping(anyString())).thenReturn(registration);
        when(registration.allowedOrigins(anyString(), anyString(), anyString())).thenReturn(registration);
        when(registration.allowedMethods(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(registration);
        when(registration.allowedHeaders(anyString())).thenReturn(registration);
        when(registration.allowCredentials(anyBoolean())).thenReturn(registration);
        when(registration.maxAge(anyLong())).thenReturn(registration);

        webConfig.addCorsMappings(registry);

        verify(registry).addMapping("/orders/**");
        verify(registration).allowedOrigins("http://localhost:3000", "http://localhost:8080", "https://api.example.com");
        verify(registration).allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
        verify(registration).allowedHeaders("*");
        verify(registration).allowCredentials(true);
        verify(registration).maxAge(3600);
    }
}
