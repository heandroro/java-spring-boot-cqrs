package com.company.orders.controller;

import com.company.orders.dto.OrderDto;
import com.company.orders.dto.OrderListResponse;
import com.company.orders.service.OrderQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order query operations")
public class OrderQueryController {

    private final OrderQuery orderQuery;

    @GetMapping
    @Operation(summary = "List orders", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<OrderListResponse> listOrders(
            @Parameter(description = "Maximum number of orders to return")
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            @Parameter(description = "Number of orders to skip")
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @Parameter(description = "Filter by order status")
            @RequestParam(required = false) String status,
            Authentication authentication) {
        
        UUID customerId = extractCustomerId(authentication);
        boolean isAdmin = isAdmin(authentication);
        
        OrderListResponse response = orderQuery.listOrders(customerId, isAdmin, limit, offset, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<OrderDto> getOrder(
            @Parameter(description = "Order ID")
            @PathVariable UUID orderId,
            Authentication authentication) {
        
        UUID customerId = extractCustomerId(authentication);
        boolean isAdmin = isAdmin(authentication);
        
        OrderDto order = orderQuery.getOrder(orderId, customerId, isAdmin);
        return ResponseEntity.ok(order);
    }

    private UUID extractCustomerId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return UUID.randomUUID();
        }
        
        String principal = authentication.getName();
        try {
            return UUID.fromString(principal);
        } catch (IllegalArgumentException e) {
            return UUID.randomUUID();
        }
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(auth -> auth.equals("ROLE_ADMIN") || auth.equals("admin"));
    }
}
