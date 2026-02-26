package com.company.orders.controller;

import com.company.orders.model.CreateOrderRequest;
import com.company.orders.model.OrderDto;
import com.company.orders.model.OrderListResponse;
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
@Tag(name = "Orders", description = "Order management operations")
public class OrderController implements OrderControllerApi {

    private final OrderService service;

    @PostMapping
    @Operation(
        summary = "Criar novo pedido",
        description = "Cria um novo pedido no sistema. Requer JWT token com escopo orders:write",
        security = @SecurityRequirement(name = "BearerAuth", scopes = {"orders:write"})
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Validação falhou"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    public ResponseEntity<OrderDto> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        
        UUID customerId = extractCustomerId(authentication, request.getCustomerId());
        OrderDto created = service.createOrder(request, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(
        summary = "Listar pedidos do usuário",
        description = "Lista pedidos do usuário autenticado com suporte a paginação e filtro",
        security = @SecurityRequirement(name = "BearerAuth", scopes = {"orders:read"})
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de pedidos"),
        @ApiResponse(responseCode = "400", description = "Parâmetro inválido"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<OrderListResponse> listOrders(
            @Parameter(description = "Quantidade de resultados (máximo 100)")
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            
            @Parameter(description = "Offset para paginação (0-indexed)")
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            
            @Parameter(description = "Filtrar por status do pedido")
            @RequestParam(required = false) String status,
            
            Authentication authentication) {
        
        UUID customerId = extractCustomerId(authentication);
        boolean isAdmin = isAdmin(authentication);
        
        OrderListResponse response = service.listOrders(customerId, isAdmin, limit, offset, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    @Operation(
        summary = "Obter detalhes do pedido",
        description = "Retorna informações completas de um pedido. User comum só pode acessar seus próprios pedidos",
        security = @SecurityRequirement(name = "BearerAuth", scopes = {"orders:read"})
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detalhes do pedido"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public ResponseEntity<OrderDto> getOrder(
            @Parameter(description = "UUID do pedido") @PathVariable UUID orderId,
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
