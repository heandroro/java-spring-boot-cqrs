package com.company.orders.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private ErrorCode code;
    private String message;
    private String path;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime timestamp;
    
    private String traceId;
    private List<String> details;

    public ErrorResponse(ErrorCode code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = OffsetDateTime.now();
    }

    public ErrorResponse(ErrorCode code, String message, String path) {
        this.code = code;
        this.message = message;
        this.path = path;
        this.timestamp = OffsetDateTime.now();
    }

    public ErrorResponse(ErrorCode code, String message, String path, String traceId) {
        this.code = code;
        this.message = message;
        this.path = path;
        this.timestamp = OffsetDateTime.now();
        this.traceId = traceId;
    }

    public enum ErrorCode {
        VALIDATION_ERROR,
        AUTHENTICATION_ERROR,
        AUTHORIZATION_ERROR,
        NOT_FOUND,
        RATE_LIMIT_EXCEEDED,
        INTERNAL_ERROR
    }
}
