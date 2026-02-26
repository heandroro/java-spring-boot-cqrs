package com.company.orders.model;

import com.company.orders.dto.ErrorResponse;
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
        ErrorResponse response = new ErrorResponse(ErrorResponse.ErrorCode.VALIDATION_ERROR, "Validation failed");
        
        assertEquals(ErrorResponse.ErrorCode.VALIDATION_ERROR, response.getCode());
        assertEquals("Validation failed", response.getMessage());
        assertNotNull(response.getTimestamp());
        assertNull(response.getPath());
        assertNull(response.getTraceId());
        assertNull(response.getDetails());
    }

    @Test
    @DisplayName("Should create ErrorResponse with code, message and path")
    void createWithCodeMessageAndPath() {
        ErrorResponse response = new ErrorResponse(
            ErrorResponse.ErrorCode.NOT_FOUND, 
            "Resource not found", 
            "/api/orders/123"
        );
        
        assertEquals(ErrorResponse.ErrorCode.NOT_FOUND, response.getCode());
        assertEquals("Resource not found", response.getMessage());
        assertEquals("/api/orders/123", response.getPath());
        assertNotNull(response.getTimestamp());
        assertNull(response.getTraceId());
        assertNull(response.getDetails());
    }

    @Test
    @DisplayName("Should create ErrorResponse with code, message, path and traceId")
    void createWithCodeMessagePathAndTraceId() {
        ErrorResponse response = new ErrorResponse(
            ErrorResponse.ErrorCode.INTERNAL_ERROR, 
            "Internal server error", 
            "/api/orders",
            "trace-123"
        );
        
        assertEquals(ErrorResponse.ErrorCode.INTERNAL_ERROR, response.getCode());
        assertEquals("Internal server error", response.getMessage());
        assertEquals("/api/orders", response.getPath());
        assertEquals("trace-123", response.getTraceId());
        assertNotNull(response.getTimestamp());
        assertNull(response.getDetails());
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
        
        assertEquals(ErrorResponse.ErrorCode.AUTHORIZATION_ERROR, response.getCode());
        assertEquals("Access denied", response.getMessage());
        assertEquals("/api/orders", response.getPath());
        assertEquals(now, response.getTimestamp());
        assertEquals("trace-456", response.getTraceId());
        assertEquals(details, response.getDetails());
        assertEquals(2, response.getDetails().size());
    }

    @Test
    @DisplayName("Should create ErrorResponse with no args constructor")
    void createWithNoArgsConstructor() {
        ErrorResponse response = new ErrorResponse();
        
        assertNull(response.getCode());
        assertNull(response.getMessage());
        assertNull(response.getPath());
        assertNull(response.getTimestamp());
        assertNull(response.getTraceId());
        assertNull(response.getDetails());
    }

    @Test
    @DisplayName("Should set and get all fields")
    void setAndGetAllFields() {
        ErrorResponse response = new ErrorResponse();
        OffsetDateTime now = OffsetDateTime.now();
        List<String> details = Arrays.asList("Detail 1");
        
        response.setCode(ErrorResponse.ErrorCode.RATE_LIMIT_EXCEEDED);
        response.setMessage("Rate limit exceeded");
        response.setPath("/api/orders");
        response.setTimestamp(now);
        response.setTraceId("trace-789");
        response.setDetails(details);
        
        assertEquals(ErrorResponse.ErrorCode.RATE_LIMIT_EXCEEDED, response.getCode());
        assertEquals("Rate limit exceeded", response.getMessage());
        assertEquals("/api/orders", response.getPath());
        assertEquals(now, response.getTimestamp());
        assertEquals("trace-789", response.getTraceId());
        assertEquals(details, response.getDetails());
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
        ErrorResponse response = new ErrorResponse(
            ErrorResponse.ErrorCode.VALIDATION_ERROR,
            "Validation failed"
        );
        
        String toString = response.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("VALIDATION_ERROR"));
        assertTrue(toString.contains("Validation failed"));
    }
}
