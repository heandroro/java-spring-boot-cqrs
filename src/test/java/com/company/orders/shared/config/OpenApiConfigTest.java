package com.company.orders.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OpenApiConfig Tests")
class OpenApiConfigTest {

    @Test
    @DisplayName("Should create OpenAPI bean with correct configuration")
    void shouldCreateOpenAPIBean() {
        OpenApiConfig openApiConfig = new OpenApiConfig();
        
        OpenAPI openAPI = openApiConfig.ordersOpenAPI();
        
        assertNotNull(openAPI);
        
        Info info = openAPI.getInfo();
        assertNotNull(info);
        assertEquals("Orders Service API", info.getTitle());
        assertEquals("Orders Service API 3.1.0 - Spring Boot 4.0.0", info.getDescription());
        assertEquals("3.1.0", info.getVersion());
        assertNotNull(info.getLicense());
        assertEquals("Apache 2.0", info.getLicense().getName());
        assertEquals("http://springdoc.org", info.getLicense().getUrl());
        
        assertNotNull(openAPI.getSecurity());
        assertFalse(openAPI.getSecurity().isEmpty());
        
        assertNotNull(openAPI.getComponents());
        assertNotNull(openAPI.getComponents().getSecuritySchemes());
        assertTrue(openAPI.getComponents().getSecuritySchemes().containsKey("BearerAuth"));
        
        SecurityScheme bearerAuth = openAPI.getComponents().getSecuritySchemes().get("BearerAuth");
        assertEquals(SecurityScheme.Type.HTTP, bearerAuth.getType());
        assertEquals("bearer", bearerAuth.getScheme());
        assertEquals("JWT", bearerAuth.getBearerFormat());
    }
}
