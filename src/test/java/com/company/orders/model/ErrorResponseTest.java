package com.company.orders.model;

import com.company.orders.shared.model.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ErrorResponse Model Tests")
class ErrorResponseTest {

    @Test
    @DisplayName("Should create ErrorResponse with code and message")
    void createWithCodeAndMessage() {
        ErrorResponse response = ErrorResponse.of(ErrorResponse.ErrorCode.VALIDATION_ERROR, "Validation failed");
        
        assertEquals(ErrorResponse.ErrorCode.VALIDATION_ERROR, response.code());
        assertEquals("Validation failed", response.message());
        assertNotNull(response.timestamp());
        assertNull(response.path());
        assertNull(response.traceId());
        assertNull(response.details());
    }

    @Test
    @DisplayName("Should create ErrorResponse with code, message and path")
    void createWithCodeMessageAndPath() {
        ErrorResponse response = ErrorResponse.of(
            ErrorResponse.ErrorCode.NOT_FOUND, 
            "Resource not found", 
            "/api/orders/123"
        );
        
        assertEquals(ErrorResponse.ErrorCode.NOT_FOUND, response.code());
        assertEquals("Resource not found", response.message());
        assertEquals("/api/orders/123", response.path());
        assertNotNull(response.timestamp());
        assertNull(response.traceId());
        assertNull(response.details());
    }

    @Test
    @DisplayName("Should create ErrorResponse with code, message, path and traceId")
    void createWithCodeMessagePathAndTraceId() {
        ErrorResponse response = ErrorResponse.of(
            ErrorResponse.ErrorCode.INTERNAL_ERROR, 
            "Internal server error", 
            "/api/orders",
            "trace-123"
        );
        
        assertEquals(ErrorResponse.ErrorCode.INTERNAL_ERROR, response.code());
        assertEquals("Internal server error", response.message());
        assertEquals("/api/orders", response.path());
        assertEquals("trace-123", response.traceId());
        assertNotNull(response.timestamp());
        assertNull(response.details());
    }

    @Test
    @DisplayName("Should create ErrorResponse with all args constructor")
    void createWithAllArgsConstructor() {
        OffsetDateTime now = OffsetDateTime.now();
        List<String> details = Arrays.asList("Error 1", "Error 2");
        
        ErrorResponse response = new ErrorResponse(
            ErrorResponse.ErrorCode.AUTHORIZATION_ERROR,
            "Access denied",
            "/api/orders",
            now,
            "trace-456",
            details
        );
        
        assertEquals(ErrorResponse.ErrorCode.AUTHORIZATION_ERROR, response.code());
        assertEquals("Access denied", response.message());
        assertEquals("/api/orders", response.path());
        assertEquals(now, response.timestamp());
        assertEquals("trace-456", response.traceId());
        assertEquals(details, response.details());
        assertEquals(2, response.details().size());
    }

    @Test
    @DisplayName("Should create ErrorResponse with null timestamp gets auto-filled")
    void createWithNullTimestampGetsAutoFilled() {
        ErrorResponse response = new ErrorResponse(
            ErrorResponse.ErrorCode.VALIDATION_ERROR,
            "Test message",
            null,
            null,
            null,
            null
        );
        
        assertEquals(ErrorResponse.ErrorCode.VALIDATION_ERROR, response.code());
        assertEquals("Test message", response.message());
        assertNull(response.path());
        assertNotNull(response.timestamp()); // Auto-filled by compact constructor
        assertNull(response.traceId());
        assertNull(response.details());
    }

    @Test
    @DisplayName("Should create ErrorResponse with all fields using constructor")
    void createWithAllFields() {
        OffsetDateTime now = OffsetDateTime.now();
        List<String> details = Arrays.asList("Detail 1");
        
        ErrorResponse response = new ErrorResponse(
            ErrorResponse.ErrorCode.RATE_LIMIT_EXCEEDED,
            "Rate limit exceeded",
            "/api/orders",
            now,
            "trace-789",
            details
        );
        
        assertEquals(ErrorResponse.ErrorCode.RATE_LIMIT_EXCEEDED, response.code());
        assertEquals("Rate limit exceeded", response.message());
        assertEquals("/api/orders", response.path());
        assertEquals(now, response.timestamp());
        assertEquals("trace-789", response.traceId());
        assertEquals(details, response.details());
    }

    @Test
    @DisplayName("Should verify all ErrorCode enum values exist")
    void verifyErrorCodeEnumValues() {
        ErrorResponse.ErrorCode[] codes = ErrorResponse.ErrorCode.values();
        
        assertEquals(6, codes.length);
        assertTrue(Arrays.asList(codes).contains(ErrorResponse.ErrorCode.VALIDATION_ERROR));
        assertTrue(Arrays.asList(codes).contains(ErrorResponse.ErrorCode.AUTHENTICATION_ERROR));
        assertTrue(Arrays.asList(codes).contains(ErrorResponse.ErrorCode.AUTHORIZATION_ERROR));
        assertTrue(Arrays.asList(codes).contains(ErrorResponse.ErrorCode.NOT_FOUND));
        assertTrue(Arrays.asList(codes).contains(ErrorResponse.ErrorCode.RATE_LIMIT_EXCEEDED));
        assertTrue(Arrays.asList(codes).contains(ErrorResponse.ErrorCode.INTERNAL_ERROR));
    }

    @Test
    @DisplayName("Should get ErrorCode by name")
    void getErrorCodeByName() {
        ErrorResponse.ErrorCode code = ErrorResponse.ErrorCode.valueOf("VALIDATION_ERROR");
        assertEquals(ErrorResponse.ErrorCode.VALIDATION_ERROR, code);
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void testEqualsAndHashCode() {
        OffsetDateTime now = OffsetDateTime.now();
        ErrorResponse response1 = new ErrorResponse(
            ErrorResponse.ErrorCode.NOT_FOUND,
            "Not found",
            "/api/test",
            now,
            "trace-1",
            null
        );
        
        ErrorResponse response2 = new ErrorResponse(
            ErrorResponse.ErrorCode.NOT_FOUND,
            "Not found",
            "/api/test",
            now,
            "trace-1",
            null
        );
        
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    @DisplayName("Should test toString")
    void testToString() {
        ErrorResponse response = ErrorResponse.of(
            ErrorResponse.ErrorCode.VALIDATION_ERROR,
            "Validation failed"
        );
        
        String toString = response.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("VALIDATION_ERROR"));
        assertTrue(toString.contains("Validation failed"));
    }
}
