package com.company.orders.controller;

import com.company.orders.dto.CreateOrderRequest;
import com.company.orders.dto.OrderDto;
import com.company.orders.dto.OrderListResponse;
import com.company.orders.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController implements OrderControllerApi {

    private final OrderService service;

    @Override
    public ResponseEntity<OrderDto> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        
        UUID customerId = extractCustomerId(authentication, request.getCustomerId());
        OrderDto created = service.createOrder(request, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    public ResponseEntity<OrderListResponse> listOrders(
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false) String status,
            Authentication authentication) {
        
        UUID customerId = extractCustomerId(authentication);
        boolean isAdmin = isAdmin(authentication);
        
        OrderListResponse response = service.listOrders(customerId, isAdmin, limit, offset, status);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<OrderDto> getOrder(
            @PathVariable UUID orderId,
            Authentication authentication) {
        
        UUID customerId = extractCustomerId(authentication);
        boolean isAdmin = isAdmin(authentication);
        
        OrderDto order = service.getOrder(orderId, customerId, isAdmin);
        return ResponseEntity.ok(order);
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
    
    private UUID extractCustomerId(Authentication authentication) {
        return extractCustomerId(authentication, null);
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
