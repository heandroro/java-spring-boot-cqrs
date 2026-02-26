package com.company.orders.controller;

import com.company.orders.dto.CreateOrderRequest;
import com.company.orders.dto.OrderDto;
import com.company.orders.service.OrderCreation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order creation operations")
public class OrderCreationController {

    private final OrderCreation orderCreation;

    @PostMapping
    @Operation(summary = "Create a new order", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<OrderDto> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        
        UUID customerId = extractCustomerId(authentication, request.customerId());
        OrderDto created = orderCreation.createOrder(request, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    private UUID extractCustomerId(Authentication authentication, UUID fallbackCustomerId) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return fallbackCustomerId != null ? fallbackCustomerId : UUID.randomUUID();
        }
        
        String principal = authentication.getName();
        try {
            return UUID.fromString(principal);
        } catch (IllegalArgumentException e) {
            return fallbackCustomerId != null ? fallbackCustomerId : UUID.randomUUID();
        }
    }
}
