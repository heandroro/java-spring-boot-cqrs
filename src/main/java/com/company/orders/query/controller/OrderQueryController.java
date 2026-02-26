package com.company.orders.query.controller;

import com.company.orders.query.handler.GetOrderQueryHandler;
import com.company.orders.query.handler.ListOrdersQueryHandler;
import com.company.orders.query.model.GetOrderQuery;
import com.company.orders.query.model.ListOrdersQuery;
import com.company.orders.query.model.OrderListQueryResult;
import com.company.orders.query.model.OrderQueryResult;
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

    private final GetOrderQueryHandler getOrderHandler;
    private final ListOrdersQueryHandler listOrdersHandler;

    @GetMapping
    @Operation(summary = "List orders", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<OrderListQueryResult> listOrders(
            @Parameter(description = "Maximum number of orders to return")
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            @Parameter(description = "Number of orders to skip")
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @Parameter(description = "Filter by order status")
            @RequestParam(required = false) String status,
            Authentication authentication) {
        
        UUID customerId = extractCustomerId(authentication);
        boolean isAdmin = isAdmin(authentication);
        
        ListOrdersQuery query = new ListOrdersQuery(customerId, isAdmin, limit, offset, status);
        OrderListQueryResult result = listOrdersHandler.handle(query);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<OrderQueryResult> getOrder(
            @Parameter(description = "Order ID")
            @PathVariable UUID orderId,
            Authentication authentication) {
        
        UUID customerId = extractCustomerId(authentication);
        boolean isAdmin = isAdmin(authentication);
        
        GetOrderQuery query = new GetOrderQuery(orderId, customerId, isAdmin);
        OrderQueryResult result = getOrderHandler.handle(query);
        return ResponseEntity.ok(result);
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
