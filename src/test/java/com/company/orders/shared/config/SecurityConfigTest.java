package com.company.orders.shared.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {

    @Test
    @DisplayName("Should create password encoder bean")
    void shouldCreatePasswordEncoder() {
        SecurityConfig securityConfig = new SecurityConfig();
        
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        
        assertNotNull(encoder);
        String encoded = encoder.encode("test");
        assertTrue(encoder.matches("test", encoded));
    }

    @Test
    @DisplayName("Should create authentication manager bean")
    void shouldCreateAuthenticationManager() throws Exception {
        SecurityConfig securityConfig = new SecurityConfig();
        AuthenticationConfiguration config = mock(AuthenticationConfiguration.class);
        AuthenticationManager mockManager = mock(AuthenticationManager.class);
        
        when(config.getAuthenticationManager()).thenReturn(mockManager);
        
        AuthenticationManager result = securityConfig.authenticationManager(config);
        
        assertNotNull(result);
        assertEquals(mockManager, result);
        verify(config).getAuthenticationManager();
    }
}
