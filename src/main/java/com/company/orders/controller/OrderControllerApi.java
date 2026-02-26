package com.company.orders.controller;

import com.company.orders.model.CreateOrderRequest;
import com.company.orders.model.OrderDto;
import com.company.orders.model.OrderListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Orders", description = "Order management operations")
public interface OrderControllerApi {

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
    @PostMapping
    ResponseEntity<OrderDto> createOrder(@RequestBody CreateOrderRequest request, Authentication authentication);

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
    @GetMapping
    ResponseEntity<OrderListResponse> listOrders(
            @Parameter(description = "Quantidade de resultados (máximo 100)")
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            @Parameter(description = "Offset para paginação (0-indexed)")
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @Parameter(description = "Filtrar por status do pedido")
            @RequestParam(required = false) String status,
            Authentication authentication);

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
    @GetMapping("/{orderId}")
    ResponseEntity<OrderDto> getOrder(
            @Parameter(description = "UUID do pedido") @PathVariable UUID orderId,
            Authentication authentication);
}
