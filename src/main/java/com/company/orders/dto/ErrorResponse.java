package com.company.orders.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;
import java.util.List;

public record ErrorResponse(
    ErrorCode code,
    String message,
    String path,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    OffsetDateTime timestamp,
    String traceId,
    List<String> details
) {
    // Compact constructor para garantir timestamp automático
    public ErrorResponse {
        if (timestamp == null) {
            timestamp = OffsetDateTime.now();
        }
    }

    // Factory methods para conveniência (substituem construtores customizados)
    public static ErrorResponse of(ErrorCode code, String message) {
        return new ErrorResponse(code, message, null, null, null, null);
    }

    public static ErrorResponse of(ErrorCode code, String message, String path) {
        return new ErrorResponse(code, message, path, null, null, null);
    }

    public static ErrorResponse of(ErrorCode code, String message, String path, String traceId) {
        return new ErrorResponse(code, message, path, null, traceId, null);
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
