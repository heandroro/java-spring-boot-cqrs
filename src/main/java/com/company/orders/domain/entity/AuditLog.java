package com.company.orders.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * AuditLog entity - Preparado para implementação em v2.1
 * Todas as anotações JPA foram removidas temporariamente para evitar
 * validação do Hibernate até a implementação completa do audit logging.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    private UUID id;
    private String entityType;
    private UUID entityId;
    private String operation;
    private UUID customerId;
    private Map<String, Object> details;
    private OffsetDateTime createdAt;
}
